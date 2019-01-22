package com.cloud.Config;

import com.cloud.JniPackage.FaissIndex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Beans {
    @Bean
    @Scope(value = "singleton")
    public FaissIndex getFaissindex(){
        return new FaissIndex();
    }

}
