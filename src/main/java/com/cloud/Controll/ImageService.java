package com.cloud.Controll;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.CloudHttpServer.CloudHttpServer;
import com.cloud.Dao.FaceDao;
import com.cloud.DataStruct.MyFloat;
import com.cloud.DataStruct.EngineData;
import com.cloud.DataStruct.ResultData;
import com.cloud.JniPackage.FaissIndex;
import com.cloud.JniPackage.FaissInfo;
import org.apache.commons.codec.binary.Base64;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequestMapping("/image")
public class ImageService {

    @Value("${engineurl}")
    private String url;
    @Autowired
    private FaissIndex faissIndex;
    @Autowired
    private FaceDao faceDao;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 创建人脸库
     * @param dbName
     * @return
     */

    @RequestMapping("/createfacedb")
    @ResponseBody
    public String createFaceDb(@RequestParam(value = "dbname")String dbName){
//        String createFaceDbUrl = "http://"+url+"/createTable";
//        Map<String,Object> dbinfo = new HashMap<>();
//        dbinfo.put("name",dbName);
//        JSONObject jsonObject = new JSONObject(dbinfo);
        return "create facedb success";
    }

    /**
     * 注册人脸
     * @param facename
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping("/registerface")
    @ResponseBody
    public String registerFace(@RequestParam(value="name")String facename,@RequestParam(value = "imagefile")MultipartFile file) {
        CloudHttpServer httpServer = new CloudHttpServer();
        String encodeurl = "http://"+url+"/locationEncode";
        int uniqueid[] = {-1};

        String filepath = "./img/" + file.getOriginalFilename() + String.valueOf(new Random().nextLong()) + System.currentTimeMillis() + ".jpg";

        Map<String, Object> imageInfo = new HashMap<>();
        Mat mat = null;
        try {
            mat = Imgcodecs.imdecode(new MatOfByte(file.getBytes()), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        byte[] result = Base64.encodeBase64(matOfByte.toArray());
        /**
         * database solve
         */
        imageInfo.put("image", new String(result));
        String responseData = httpServer.SendMessage(imageInfo, encodeurl);
        EngineData engineData = JSONObject.parseObject(responseData, EngineData.class);
        if (engineData.getEncodes().size() > 1)
            return "image include more than one face";

        FaissInfo faissInfo = faissIndex.searchIndex(1, engineData.getEncodes().get(0));
        if (Math.sqrt(faissInfo.distance[0]) > 1.1) {
            //距离太大，数据库无此人脸
            uniqueid[0] = faceDao.searchMaxUniqueId() + 1;

        } else {
            uniqueid[0] = (int) faissInfo.ids[0];
        }

        //从引擎获取图片的特征
        float[] encode = engineData.getEncodes().get(0);
        //转base64
        String strencode = Arrays.toString(encode);
        byte[] blob = Base64.encodeBase64(strencode.getBytes());
        //图片存到本地
        BufferedInputStream bis = null;
        FileOutputStream fr = null;
        try {
            bis = new BufferedInputStream(file.getInputStream());
            fr = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int ret = -1;
            while ((ret = bis.read(buffer, 0, buffer.length)) != -1) {
                fr.write(buffer, 0, ret);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    System.out.println("bis register face failed");
                    return "register face failed";
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//        }
            //     信息插入数据库

            faceDao.insertFace(uniqueid[0], facename, blob, filepath); //一般信息存入mysql
            faissIndex.addIndex("FaceUser", 1, encode, uniqueid,128);//人脸特征存入faiss
            return "register face success";
        }
    }


    /**
     * 根据encode找人脸
     * @param encode
     * @return
     */
    @RequestMapping(value = "/searchfaceencode",method = RequestMethod.POST)
    @ResponseBody
    public String searchFaceEncode(@org.springframework.web.bind.annotation.RequestBody MyFloat encode){
        float[] floats = encode.getEncode();
        for(int i=0;i<floats.length;i++)
            System.out.println(floats[i]);
        FaissInfo faissInfo = faissIndex.searchIndex(1,floats);
        long uniqueid = faissInfo.ids[0];
        float distance = faissInfo.distance[0];
        if(Math.sqrt(distance)>1.1)
            return "unknown";
        String result = JSON.toJSONString(faceDao.searchDb((int)uniqueid),true);
        return result;
    }

    /**
     * 根据图片找人脸
     * @param file
     * @return
     */
    @RequestMapping("/searchfaceimage")
    @ResponseBody
    public String searchFaceImage(@RequestParam(value = "imagefile") MultipartFile file){
        Mat mat = null;
        String encodeurl = "http://"+url+"/locationEncode";
        CloudHttpServer httpServer = new CloudHttpServer();
        Set<String> personNames = new HashSet<>();
        try {
            mat = Imgcodecs.imdecode(new MatOfByte(file.getBytes()),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",mat,matOfByte);
        byte[] result = Base64.encodeBase64(matOfByte.toArray());
        Map<String,Object> imageInfo = new HashMap<>();
        imageInfo.put("image",new String(result));
        String responseData = httpServer.SendMessage(imageInfo,encodeurl);
        if(responseData.equals("null")){
            return "search failed";
        }
        EngineData engineData = JSONObject.parseObject(responseData,EngineData.class);
        int picNum = engineData.getEncodes().size();
        float[] searchEncodes = new float[picNum*128];
        for(int i=0;i<picNum;i++)
            for(int j =0;j<128;j++)
                searchEncodes[128*i+j] = engineData.getEncodes().get(i)[j];
        FaissInfo faissInfo = faissIndex.searchIndex(picNum,searchEncodes);

        for(int i=0;i<picNum;i++){
            if(Math.sqrt((double)faissInfo.distance[i*10])>1.1) {
                continue;
            }
            else{
                List<Map<String, Object>> mapList = faceDao.searchDb((int)faissInfo.ids[i*10]);
                if(mapList == null)
                    continue;
                for(Map<String,Object> map : mapList){
                    String personName = String.valueOf(map.get("NAME"));
                    personNames.add(personName);
                }
            }
        }
        if(personNames.isEmpty())
            return "no people in this picture";
        return JSON.toJSONString(personNames);
    }

    @RequestMapping("/deleteface")
    @ResponseBody
    public String deleteFace(@RequestParam(value = "image") MultipartFile file){
        Mat mat = null;
        String encodeurl = "http://"+url+"/locationEncode";
        CloudHttpServer httpServer = new CloudHttpServer();
        try {
            mat = Imgcodecs.imdecode(new MatOfByte(file.getBytes()),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",mat,matOfByte);
        byte[] result = Base64.encodeBase64(matOfByte.toArray());
        Map<String,Object> imageInfo = new HashMap<>();
        imageInfo.put("image",new String(result));
        String responseData = httpServer.SendMessage(imageInfo,encodeurl);
        if(responseData.equals("null")){
            return "search failed";
        }
        EngineData engineData = JSONObject.parseObject(responseData,EngineData.class);
        int picNum = engineData.getEncodes().size();
        float[] searchEncodes = new float[picNum*128];
        for(int i=0;i<picNum;i++)
            for(int j =0;j<128;j++)
                searchEncodes[128*i+j] = engineData.getEncodes().get(i)[j];
        FaissInfo faissInfo = faissIndex.searchIndex(picNum,searchEncodes);
        List<Integer> deleteIds = new ArrayList<>();
        if(faissInfo.distance.length<1)
            return "can't find this person";
        for(int i=0;i<faissInfo.distance.length;i++){
            if(Math.sqrt(faissInfo.distance[i])<1.1){
                deleteIds.add((int)faissInfo.ids[i]);
            }
        }
        if(deleteIds.size()<=0)
            return "can't find this person";
        for(int id : deleteIds){
            List<Map<String,Object>> idsInfo = faceDao.searchDb(id);
            for(Map<String,Object> idInfo:idsInfo){
                String filepath = idInfo.get("IMAGEPATH").toString();
                try{
                    File deletefile = new File(filepath);
                    deletefile.delete();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            faceDao.deleteFace(id);
            faissIndex.deleteIndex(id,"FaceUser");
        }

        return "delete success";
    }

    /**
     *
     * @param file
     * @return
     */
    @RequestMapping("/predict")
    @ResponseBody
    public  String  predict(@RequestParam(value = "image") MultipartFile file){

        return null;
    }


}
