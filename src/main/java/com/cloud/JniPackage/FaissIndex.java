package com.cloud.JniPackage;
import com.cloud.JniPackage.FaissInfo;
public class FaissIndex {
//    static {
//        System.loadLibrary("FaissIndex");
//    }

    public native void createIndex(String indexName, int dim);

    public native void addIndex(String indexName, int num, float[] feature, int[] uniqueid, int dim);

    public native FaissInfo searchIndex(int num, float[] feature,int dim);

    public native void deleteIndex(int uniqueId, String indexName);

}