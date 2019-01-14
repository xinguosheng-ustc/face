package com.cloud.Dao;

import com.alibaba.fastjson.JSON;
import com.cloud.plugin.BlobtoByte;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.util.List;
import java.util.Map;

@Repository
public class FaceDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试
     * @param tablename
     */
    public void test(String tablename){
        String sql = "select ENCODING from "+tablename+" where UNIQUEID=0";
        Blob blob = jdbcTemplate.queryForObject(sql,Blob.class);
        byte[] bytes = BlobtoByte.blobToBytes(blob);
        byte[] bytes1 = Base64.decodeBase64(bytes);
        System.out.print("end::");
        System.out.println(new String(bytes1));

    }
    /*
          注册人脸到数据库
     */
    public int insertFace(int uniquedId,String name,byte[] blob,String path){
        String sql = "insert into USER (UNIQUEID,NAME,ENCODING,IMAGEPATH) value (?,?,?,?)";
        Object args[] = {uniquedId,name,blob,path};
        int temp = jdbcTemplate.update(sql,args);
        if(temp>0){
            return 0;
        }else{
            return 1;
        }

    }
    public int deleteFace(int uniqueId){
        String sql = "delete from USER where UNIQUEID = ?";
        Object args[] = {uniqueId};
        int temp = jdbcTemplate.update(sql,args);
        return temp;
    }
    /*
        找最大的uniqueid
     */
    public int searchMaxUniqueId(){
        String sql = "select Max(UNIQUEID) from USER";
        int maxid = jdbcTemplate.queryForObject(sql,Integer.class);
        return maxid;
    }
    /*
        找到对应的uniqueid
     */
    public List<Map<String, Object>> searchDb(int uniqueId){
        String sql = "select * from USER"+" where UNIQUEID ="+uniqueId;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if(list.isEmpty())
            return null;
//        String value = JSON.toJSONString(list);
        return list;
    }
    /*

     */


}
