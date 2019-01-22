package com.cloud.Controll;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.CloudHttpServer.CloudHttpServer;
import com.cloud.Dao.FaceDao;
import com.cloud.Components.DeviceInfoAll;
import com.cloud.DataStruct.MyFloat;
import com.cloud.DataStruct.EngineData;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/image")
public class ImageService {
    @Value("${flag}")
    private double flag;
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
    public String registerFace(@RequestParam(value="name")String facename,@RequestParam(value = "imagefile")MultipartFile file,@RequestParam(value="workunit")String workunit,@RequestParam(value = "sex")String sex,@RequestParam(value = "occupation")String occupation) {
        CloudHttpServer httpServer = new CloudHttpServer();
        String encodeurl = "http://"+url+"/locationEncode";
        int uniqueid[] = {-1};
        if(workunit.equals(null))
            return "workunit is null";

        if(sex.equals(null))
            return "sex is null";

        if(occupation.equals(null))
            return "occupation is null";
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

        if (faissInfo.distance[0] <flag) {
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

            int ret =  faceDao.insertFace(uniqueid[0], facename, blob, filepath,workunit,sex,occupation); //一般信息存入mysql
            if(ret ==1 )
                return "register face failed";
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
        FaissInfo faissInfo = faissIndex.searchIndex(1,floats);
        long uniqueid = faissInfo.ids[0];
        float distance = faissInfo.distance[0];
        if(distance<flag)
            return "unknown";
        List<Map<String,Object>> lists = faceDao.searchDb((int)uniqueid);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdf.format(date);//将时间格式转换成符合Timestamp要求的格式.
        Timestamp dates =Timestamp.valueOf(nowTime);//把时间转换
        faceDao.updateTimestamp((int)uniqueid,dates);
        Map<String,String> result = new HashMap<>();
        for(Map<String,Object> map : lists){
            String personName = String.valueOf(map.get("NAME"));
            String personWorkunit = String.valueOf(map.get("WORKUNIT"));
            String personSex = String.valueOf(map.get("SEX"));
            String personOccupation = String.valueOf(map.get("OCCUPATION"));
            result.put("name",personName);
            result.put("sex",personSex);
            result.put("company",personWorkunit);
            result.put("job",personOccupation);
        }
        return JSON.toJSONString(result);
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
        Set<String> personsInfo = new HashSet<>();
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

            if((double)faissInfo.distance[i*10]<flag) {
                continue;
            }
            else{
                List<Map<String, Object>> mapList = faceDao.searchDb((int)faissInfo.ids[i*10]);
                if(mapList == null)
                    continue;
                for(Map<String,Object> map : mapList){
                    String personName = String.valueOf(map.get("NAME"));
                    String personWorkunit = String.valueOf(map.get("WORKUNIT"));
                    String personSex = String.valueOf(map.get("SEX"));
                    String personOccupation = String.valueOf(map.get("OCCUPATION"));
                    personsInfo.add(personName+","+personWorkunit+","+personSex+","+personOccupation);
                }
            }
        }
        if(personsInfo.isEmpty())
            return "no people in this picture";

        return JSON.toJSONString(personsInfo);
    }

    /**
     *
     * 删除人脸
     * @param file
     * @return
     */
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
        Set<Integer> deleteIds = new HashSet<>();
        if(faissInfo.distance.length<1)
            return "can't find this person";
        for(int i=0;i<faissInfo.distance.length;i++){
            if(faissInfo.distance[i]>flag){
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
     * 人脸比对
     * @param files
     * @return
     */
    @RequestMapping("/predict")
    @ResponseBody
    public  String  predict(@RequestParam(value = "imagefile") List<MultipartFile> files){
        Mat mat = null;
        String encodeurl = "http://"+url+"/locationEncode";
        List<float[]> fileFeature = new ArrayList<>();
        if(files.size()>2)
            return "picture num more than 2";
        if(files.size()<2)
            return "picture num less than 2";
        for(MultipartFile file:files){
            try(CloudHttpServer httpServer = new CloudHttpServer()) {
                mat = Imgcodecs.imdecode(new MatOfByte(file.getBytes()),1);
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
                if(engineData.getEncodes().size()>1)
                    return  file.getOriginalFilename()+" has more than one person";
                if(engineData.getEncodes().size()<1)
                    return file.getOriginalFilename()+" has no person";
                fileFeature.add(engineData.getEncodes().get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        float[] fPic1 = fileFeature.get(0);
        float[] fPic2 = fileFeature.get(1);
            double fenzi=0;
            for(int i=0;i<fPic1.length;i++){
                fenzi+=fPic1[i]*fPic2[i];
            }

            double left=0;
            double right=0;
            for(int i=0;i<fPic1.length;i++){
                left+=fPic1[i]*fPic1[i];
                right+=fPic2[i]*fPic2[i];
            }

            double result=fenzi/Math.sqrt(left*right);
            return String.valueOf(result);
    }
}
