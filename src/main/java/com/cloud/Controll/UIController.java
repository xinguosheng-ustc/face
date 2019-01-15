package com.cloud.Controll;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UIController {
    @RequestMapping("/ui")
    public String faceUi(){
        return "index";
    }
}
