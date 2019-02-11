package com.cloud.Dao;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class Test {
    @Autowired
    private FaceDao faceDao;
    @RequestMapping("/test1")
    public String test(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdf.format(date);//将时间格式转换成符合Timestamp要求的格式.
        Timestamp dates =Timestamp.valueOf(nowTime);//把时间转换
        faceDao.updateTimestamp(50,dates);
        return "only a test";
    }
    @RequestMapping("test2")
    public String test2(){
        List<Map<String, Object>> infoMap = null;
        try {
            infoMap = faceDao.searchDb(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String,Object> info =  infoMap.get(0);
        Timestamp timestamp = (Timestamp)info.get("LASTTIME");
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdf.format(date);//将时间格式转换成符合Timestamp要求的格式.
        return nowTime;
    }
}
