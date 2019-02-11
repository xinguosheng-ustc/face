package com.cloud.Components;

import com.cloud.DataStruct.DeviceInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Scope("singleton")
@Component
public class DeviceInfoAll {
    private CopyOnWriteArrayList<DeviceInfo> deviceInfos;
    public static boolean isRun;
    private Thread keepAliveTime;
    public DeviceInfoAll(){
        deviceInfos = new CopyOnWriteArrayList<DeviceInfo>();
        isRun = false;
    }


    public CopyOnWriteArrayList<DeviceInfo> getDeviceInfos() {
        return deviceInfos;
    }

    public void setDeviceInfos(CopyOnWriteArrayList<DeviceInfo> deviceInfos) {
        this.deviceInfos = deviceInfos;
    }

    public Thread getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(Thread keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
    public synchronized int start(){
        if(isRun ==false) {
            isRun = true;
            keepAliveTime = new Thread(() -> {
                    while (isRun) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                        for(DeviceInfo deviceInfo:deviceInfos){
                            if(deviceInfo.getKeepAlive()<=0)
                                deviceInfos.remove(deviceInfo);
                            deviceInfo.setKeepAlive(deviceInfo.getKeepAlive()-1);
                            deviceInfo.setAliveTime(deviceInfo.getAliveTime()+1);
                        }
                    }

            });
            keepAliveTime.start();

        }else{
            return 1;
        }
        return 0;
    }
    public synchronized int stop(){
        if(isRun == true) {
            isRun = false;
            if(keepAliveTime.isAlive()) {
                    System.out.println("interup");
                    keepAliveTime.interrupt();
                }
                deviceInfos.clear();
            return 0;
            }
        else{
            return 1;
        }
    }
    public synchronized boolean isRun(){
         return isRun;
    }

}

