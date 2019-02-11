package com.cloud.Controll;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.CloudHttpServer.CloudHttpServer;
import com.cloud.Dao.FaceDao;
import com.cloud.Components.DeviceInfoAll;
import com.cloud.DataStruct.MyFloat;
import com.cloud.DataStruct.EngineData;
import com.cloud.DataStruct.PeopleInfo;
import com.cloud.JniPackage.FaissIndex;
import com.cloud.JniPackage.FaissInfo;
import com.cloud.plugin.ShowImage;
import org.apache.commons.codec.binary.Base64;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
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

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_UNCHANGED;

@Controller
@RequestMapping("/image")
public class FaceService {
    @Value("${dim}")
    private int dim;
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
        String encodeurl = "http://"+url+"/locationEncode";
        int uniqueid[] = {-1};
        String filepath = "./img/" + file.getOriginalFilename() + String.valueOf(new Random().nextLong()) + System.currentTimeMillis() + ".jpg";
        Map<String,Object> imageInfo = new HashMap<>();
        CloudHttpServer httpServer = new CloudHttpServer();
        //123
        if(facename == null|| file == null || workunit == null || sex == null ||occupation == null)
            return "5";
        Mat borderMat = preProcess(file);
        //123
//        Mat borderMat = preProcess(file);
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",borderMat,matOfByte);
        byte[] result = Base64.encodeBase64(matOfByte.toArray());
        imageInfo.put("image",new String(result));
        String responseData = null;
        try {
            responseData = httpServer.SendMessage(imageInfo,encodeurl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        EngineData engineData = JSONObject.parseObject(responseData,EngineData.class);
        if (engineData.getEncodes().size() > 1)
            return "1";
        if (engineData.getEncodes().size() <= 0)
            return "2";
        FaissInfo faissInfo = faissIndex.searchIndex(1, engineData.getEncodes().get(0),dim);
        if (faissInfo.distance[0] <flag) {
                //距离太大，数据库无此人脸
            uniqueid[0] = faceDao.searchMaxUniqueId() + 1;
        } else {
            uniqueid[0] = (int) faissInfo.ids[0];
        }

            //从引擎获取图片的特征
        float[] encode = engineData.getEncodes().get(0);
            //转base64
//        String strencode = Arrays.toString(encode);
//        byte[] blob = Base64.encodeBase64(strencode.getBytes());
            //     信息插入数据库
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdf.format(date);//将时间格式转换成符合Timestamp要求的格式.
        Timestamp dates =Timestamp.valueOf(nowTime);//把时间转换
        int retInsert =  faceDao.insertFace(uniqueid[0], facename, filepath,workunit,sex,occupation,dates); //一般信息存入mysql
        if(retInsert ==1 )
            return "3";
        faissIndex.addIndex("FaceUser", 1, encode, uniqueid,dim);//人脸特征存入faiss

        boolean ret = FileStore(file,filepath);
        if(ret == false)
            return "4";

        return "0";
    }

    /**
     *  批量注册人脸
     */
    @RequestMapping("/registerbatch")
    @ResponseBody
    public String registerBatch(@RequestParam("imagefile") List<MultipartFile> files) {
        int num = files.size();
        System.out.println(files.size());
        for(MultipartFile file : files) {
            Mat mat = null;
            String filename = file.getOriginalFilename();
            int index = filename.indexOf(".");
            String str = filename.substring(0, index);
            String[] strs = str.split("_");

            String occupation = strs[0];
            String facename = strs[1];
            String sex = "";
            String workunit="";
//            String sex = strs[2];
//            String workunit = strs[3];
//            if(facename.equals(null)|| file.equals(null) || workunit.equals(null) || sex.equals(null) ||occupation.equals(null))
//                return "5";

            CloudHttpServer httpServer = new CloudHttpServer();
            String encodeurl = "http://" + url + "/locationEncode";
            int uniqueid[] = {-1};
            String filepath = "./img/" + file.getOriginalFilename() + String.valueOf(new Random().nextLong()) + System.currentTimeMillis() + ".jpg";
            Mat borderMat = preProcess(file);
            Map<String, Object> imageInfo = new HashMap<>();
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg",borderMat,matOfByte);
            byte[] result = Base64.encodeBase64(matOfByte.toArray());
            imageInfo.put("image",new String(result));
            String responseData = null;
            try {
                responseData = httpServer.SendMessage(imageInfo, encodeurl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            EngineData engineData = JSONObject.parseObject(responseData, EngineData.class);
            if (engineData.getEncodes().size() > 1)
                return "1";
            if(engineData.getEncodes().size()<=0)
                return "2";
            float[] test = engineData.getEncodes().get(0);
            FaissInfo faissInfo = faissIndex.searchIndex(1,test,dim);
            if (faissInfo.distance[0] < flag) {
                //距离太大，数据库无此人脸
                uniqueid[0] = faceDao.searchMaxUniqueId() + 1;

            } else {
                uniqueid[0] = (int) faissInfo.ids[0];
            }

            //从引擎获取图片的特征
            float[] encode = engineData.getEncodes().get(0);
            //转base64
            String strencode = Arrays.toString(encode);

                //     信息插入数据库
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTime = sdf.format(date);//将时间格式转换成符合Timestamp要求的格式.
            Timestamp dates =Timestamp.valueOf(nowTime);//把时间转换
            int ret2 = faceDao.insertFace(uniqueid[0], facename, filepath, workunit, sex, occupation,dates); //一般信息存入mysql
            if (ret2 == 1)
                return "3";
            boolean ret = FileStore(file,filepath);
            if(ret == false)
                return "4";
            faissIndex.addIndex("FaceUser", 1, encode, uniqueid, dim);//人脸特征存入faiss
        }
        return "0";
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
        FaissInfo faissInfo = faissIndex.searchIndex(1,floats,dim);
        long uniqueid = faissInfo.ids[0];
        float distance = faissInfo.distance[0];
        if(distance<flag)
            return "unknown";
        List<Map<String,Object>> lists = null;
        try {
            lists = faceDao.searchDb((int)uniqueid);
        } catch (Exception e) {
            return "unknown";
        }
        if(lists == null)
            return "unknown";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdf.format(date);//将时间格式转换成符合Timestamp要求的格式.
        Timestamp dates =Timestamp.valueOf(nowTime);//把时间转换
        faceDao.updateTimestamp((int)uniqueid,dates);
        Map<String,String> result = new HashMap<>();
        for(Map<String,Object> map : lists){
            String personName = String.valueOf(map.get("NAME"));
            if(personName ==null)
                personName ="";
            String personWorkunit = String.valueOf(map.get("WORKUNIT"));
            if(personWorkunit==null)
                personWorkunit="";
            String personSex = String.valueOf(map.get("SEX"));
            if(personSex==null)
                personSex="";
            String personOccupation = String.valueOf(map.get("OCCUPATION"));
            if(personOccupation ==null)
                personOccupation="";
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
        Set<PeopleInfo> returnInfo = new HashSet<>();
        Mat borderMat = preProcess(file);
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",borderMat,matOfByte);
        byte[] result = Base64.encodeBase64(matOfByte.toArray());
        Map<String,Object> imageInfo = new HashMap<>();
        imageInfo.put("image",new String(result));
        String responseData = null;
        try {
            responseData = httpServer.SendMessage(imageInfo,encodeurl);
        } catch (IOException e) {
            return "1";
        }
        if(responseData.equals("null")){
            return "2";
        }
        EngineData engineData = JSONObject.parseObject(responseData,EngineData.class);
        List<float[]> boxs = engineData.getBoxs();
        int picNum = engineData.getEncodes().size();
        float[] searchEncodes = new float[picNum*dim];

        for(int i=0;i<picNum;i++)
            for(int j =0;j<dim;j++)
                searchEncodes[dim*i+j] = engineData.getEncodes().get(i)[j];
        FaissInfo faissInfo = faissIndex.searchIndex(picNum,searchEncodes,dim);
        for(int i=0;i<picNum;i++){
            if((double)faissInfo.distance[i*10]<flag) {
                continue;
            }
            else{
                List<Map<String, Object>> mapList = null;
                try {
                    mapList = faceDao.searchDb((int)faissInfo.ids[i*10]);
                } catch (Exception e) {
                    continue;
                }
                if(mapList == null)
                    continue;
                for(Map<String,Object> map : mapList){
                    PeopleInfo peopleInfo = new PeopleInfo();
                    peopleInfo.setPersonName(String.valueOf(map.get("NAME")));
                    peopleInfo.setPersonOccupation(String.valueOf(map.get("OCCUPATION")));
                    peopleInfo.setPersonSex(String.valueOf(map.get("SEX")));
                    peopleInfo.setPersonWorkunit(String.valueOf(map.get("WORKUNIT")));
                    peopleInfo.setPersonLocation(boxs.get(i));
                    returnInfo.add(peopleInfo);
                }
            }
        }
        if(returnInfo.isEmpty())
            return "3";

        return JSON.toJSONString(returnInfo,true);
    }

    /**
     *
     * 删除人脸
     * @param
     * @return
     */
    @RequestMapping("/deletebyinfo")
    @ResponseBody
    public String deleteFaceinfo(@RequestParam(value = "name")String name,@RequestParam(value = "workunit")String workunit,@RequestParam(value = "sex")String sex,@RequestParam(value = "occupation")String occupation){

        if (name.equals(null) || workunit.equals(null) || sex.equals(null) ||occupation.equals(null)) {
            return "1";
        }

        int ret  = faceDao.deleteFace(name,workunit,sex,occupation);
        if(ret >=0)
            return "0";
        else
            return "2";
    }
    @RequestMapping("/deletebyimage")
    @ResponseBody
    public String deleteFace(@RequestParam(value = "imagefile") MultipartFile file){
        String encodeurl = "http://"+url+"/locationEncode";
        CloudHttpServer httpServer = new CloudHttpServer();
        Mat borderMat = preProcess(file);
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",borderMat,matOfByte);
        byte[] result = Base64.encodeBase64(matOfByte.toArray());
        Map<String,Object> imageInfo = new HashMap<>();
        imageInfo.put("image",new String(result));
        String responseData = null;
        try {
            responseData = httpServer.SendMessage(imageInfo,encodeurl);
        } catch (IOException e) {
            return "1";
        }
        if(responseData.equals("null")){
            return "search failed";
        }
        EngineData engineData = JSONObject.parseObject(responseData,EngineData.class);
        int picNum = engineData.getEncodes().size();
        float[] searchEncodes = new float[picNum*dim];
        for(int i=0;i<picNum;i++)
            for(int j =0;j<dim;j++)
                searchEncodes[dim*i+j] = engineData.getEncodes().get(i)[j];
        FaissInfo faissInfo = faissIndex.searchIndex(picNum,searchEncodes,dim);
        Set<Integer> deleteIds = new HashSet<>();
        if(faissInfo.distance.length<1)
            return "2";
        for(int i=0;i<faissInfo.distance.length;i++){
            if(faissInfo.distance[i]>flag){
                deleteIds.add((int)faissInfo.ids[i]);
            }
        }
        if(deleteIds.size()<=0)
            return "2";
        for(int id : deleteIds){
            List<Map<String,Object>> idsInfo = null;
            try {
                idsInfo = faceDao.searchDb(id);
            } catch (Exception e) {
                continue;
            }
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

        return "0";
    }

    /**
     * 人脸比对
     * @param files
     * @return
     */
    @RequestMapping("/facecompare")
    @ResponseBody
    public  String  predict(@RequestParam(value = "imagefile") List<MultipartFile> files){
        Mat mat = null;
        String encodeurl = "http://"+url+"/locationEncode";
        List<float[]> fileFeature = new ArrayList<>();
        if(files.size()>2)
            return "1";

        for(MultipartFile file:files){
            try(CloudHttpServer httpServer = new CloudHttpServer()) {
                Mat borderMat = preProcess(file);
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg",borderMat,matOfByte);
                byte[] result = Base64.encodeBase64(matOfByte.toArray());
                Map<String,Object> imageInfo = new HashMap<>();
                imageInfo.put("image",new String(result));
                String responseData = httpServer.SendMessage(imageInfo,encodeurl);
                if(responseData.equals("null")){
                    return "3";
                }
                EngineData engineData = JSONObject.parseObject(responseData,EngineData.class);
                if(engineData.getEncodes().size()>1)
                    return  "2";
                if(engineData.getEncodes().size()<1)
                    return "3";
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
    public boolean FileStore(MultipartFile file,String filepath) {
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
                    return false;
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }
    public  Mat preProcess(MultipartFile file){
        Mat mat = null;
        Mat borderMat = null;
        try {
            mat = Imgcodecs.imdecode(new MatOfByte(file.getBytes()), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int originCols = mat.cols();
        int originRows = mat.rows();
        System.out.println(file.getOriginalFilename()+":"+originCols+":"+originRows);
        borderMat = new Mat(originRows+100,originCols+100, CvType.CV_8UC3,new Scalar(255,255,255));
        Rect area = new Rect(0,0,originCols,originRows);
        Mat newBorderMat = new Mat(borderMat,area);
        mat.copyTo(newBorderMat);
        return borderMat;
    }
}

