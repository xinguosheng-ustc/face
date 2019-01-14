package com.cloud.DataStruct;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class ResultData {

    @JSONField(name ="boxs",ordinal = 10)
    private List<double[]> boxs;

    @JSONField(name ="results",ordinal = 20)
    private List<String> result;

    @JSONField(name="encodes",ordinal = 30)
    private List<double[]> encodes;

    public List<double[]> getBoxs() {
        return boxs;
    }

    public void setBoxs(List<double[]> boxs) {
        this.boxs = boxs;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    public List<double[]> getEncodes() {
        return encodes;
    }

    public void setEncodes(List<double[]> encodes) {
        this.encodes = encodes;
    }
}
