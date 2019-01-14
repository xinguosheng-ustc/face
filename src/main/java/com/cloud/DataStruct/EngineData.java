package com.cloud.DataStruct;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class EngineData {

    @JSONField(name = "boxs", ordinal = 10)
    private List<float[]> boxs;

    @JSONField(name = "encodes", ordinal = 30)
    private List<float[]> encodes;


    public List<float[]> getBoxs() {
        return boxs;
    }

    public void setBoxs(List<float[]> boxs) {
        this.boxs = boxs;
    }

    public List<float[]> getEncodes() {
        return encodes;
    }

    public void setEncodes(List<float[]> encodes) {
        this.encodes = encodes;
    }
}