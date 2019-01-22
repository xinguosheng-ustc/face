package com.cloud.DataStruct;



public class DeviceInfo {
    private String ipAddress;
    private int aliveTime;
    private int keepAlive;

    public DeviceInfo(String ipAddress, int aliveTime, int keepAlive) {
        this.ipAddress = ipAddress;
        this.aliveTime = aliveTime;
        this.keepAlive = keepAlive;
    }

    public DeviceInfo(){
        this.keepAlive = 60;
        this.aliveTime = 0;
    }
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getAliveTime() {
        return aliveTime;
    }

    public void setAliveTime(int aliveTime) {
        this.aliveTime = aliveTime;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }
}
