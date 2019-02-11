package com.cloud.Controll;

import com.alibaba.fastjson.JSON;
import com.cloud.Components.DeviceInfoAll;
import com.cloud.DataStruct.DeviceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DeviceService {
    @Autowired
    DeviceInfoAll deviceInfoAll;
    @Autowired
    private HttpServletRequest clientRequest;

    @RequestMapping("/devstart")
    public String start(){
        int ret = deviceInfoAll.start();
        if(ret == 0)
            return "success";
        else
            return "failed";
    }
    @RequestMapping("/devstop")
    public String stop(){
        int ret = deviceInfoAll.stop();
        if(ret == 0)
            return "success";
        else
            return "failed";
    }
    @RequestMapping("/devinfo")
    public  String info(){
        if(!deviceInfoAll.getDeviceInfos().isEmpty())
            return JSON.toJSONString(deviceInfoAll.getDeviceInfos());
        else
            return "null";
    }
    @RequestMapping("/heartbeat")
    public String heartBeat() {
        String remoteIp = clientRequest.getRemoteAddr();
        if (deviceInfoAll.isRun()) {
            int ret = 0;
            for(DeviceInfo deviceInfo :deviceInfoAll.getDeviceInfos()){
                    if(deviceInfo.getIpAddress().equals(remoteIp))
                        ret = 1;
            }
            if (ret == 0) {
                System.out.println("1");
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setIpAddress(remoteIp);
                deviceInfoAll.getDeviceInfos().add(deviceInfo);
            }else{
                System.out.println("2");
                for(DeviceInfo deviceInfo :deviceInfoAll.getDeviceInfos()){
                    if(deviceInfo.getIpAddress().equals(remoteIp))
                        deviceInfo.setKeepAlive(60);
                }
            }
        }
        return "";
    }
}
