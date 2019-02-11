package com.cloud;

import com.cloud.JniPackage.FaissIndex;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class Init implements CommandLineRunner {
    @Value("${dim}")
    private int dim;
    public static Logger logger = LoggerFactory.getLogger(Init.class);
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java343"); //load library
        System.loadLibrary("FaissIndex");
   }
    @Autowired
    private FaissIndex faissIndex;
    @Override
    public void run(String... args) throws Exception {
            File file = new File("./img");
            if(!file.exists()){
                file.mkdirs();
            }
            faissIndex.createIndex("FaceUser",dim);

    }
}
