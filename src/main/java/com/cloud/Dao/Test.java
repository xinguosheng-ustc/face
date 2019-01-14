package com.cloud.Dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {
    @Autowired
    private FaceDao faceDao;
    @RequestMapping("/eee")
    public String test(){
        faceDao.searchMaxUniqueId();
//        for(Map<String, Object> value : list){
//            System.out.println(value.keySet());
//        }
        return "only a test";
    }
}
