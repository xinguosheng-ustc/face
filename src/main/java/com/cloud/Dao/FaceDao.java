package com.cloud.Dao;

import com.alibaba.fastjson.JSON;
import com.cloud.plugin.BlobtoByte;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Timestamp;
import java.sql.Types;
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
    public int insertFace(int uniquedId,String name,String path,String workunit,String sex,String occupation,Timestamp date){
            if(workunit == null )
                workunit="null";
            if(sex == null)
                sex ="null";
            if(occupation==null)
                occupation="null";
            String sql = "insert into USER (UNIQUEID,NAME,IMAGEPATH,WORKUNIT,SEX,OCCUPATION,LASTTIME) value (?,?,?,?,?,?,?)";
            Object args[] = {uniquedId,name,path,workunit,sex,occupation,date};
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
    public int deleteFace(String name,String workunit,String sex,String occupation){
        String sql = "delete from USER where NAME =? and WORKUNIT = ? and SEX=? and OCCUPATION=?";
        Object args[] = {name,workunit,sex,occupation};
        int temp = jdbcTemplate.update(sql,args);
        return temp;
    }
    /*
        找最大的uniqueid
     */
    public int searchMaxUniqueId(){
        String sql = "select Max(UNIQUEID) from USER";
        Integer maxid = jdbcTemplate.queryForObject(sql,Integer.class);
        if(maxid == null)
            return 0;
        return maxid;
    }
    /*
        找到对应的uniqueid
     */
    public List<Map<String, Object>> searchDb(int uniqueId) throws Exception{
        String sql = "select * from USER"+" where UNIQUEID ="+uniqueId;
        List<Map<String, Object>> list = null;
        list = jdbcTemplate.queryForList(sql);
        if(list.isEmpty() ||list == null)
            return null;
//        String value = JSON.toJSONString(list);
        return list;
    }
    /*
        更新最后来访时间
     */
    public int updateTimestamp(int uniqueId, Timestamp timestamp){
        String sql = "update USER SET LASTTIME =? where UNIQUEID = ?";
        Object args[] = {timestamp,uniqueId};
        int temp = jdbcTemplate.update(sql,args);
        return temp;
    }
    public int searchUniqueIdbyInfo(String name,String workunit,String sex,String occupation){
        String sql = "select UNIQUEID from USER where NAME = ? and WORKUNIT = ? and SEX = ? and OCCUPATION = ?";
        Object args[] = {name,workunit,sex,occupation};
        Integer maxid = jdbcTemplate.queryForObject(sql,Integer.class);
        return maxid;
    }

}
