package com.cloud.CloudHttpServer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Component
public class CloudHttpServer implements AutoCloseable{
    private OkHttpClient client = new OkHttpClient
            .Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10,TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final MediaType JSONS = MediaType.parse("application/json; charset=utf-8");

    public String SendMessage(Map<String,Object> msgInfo,String url) throws IOException {
        JSONObject jsonObject = new JSONObject(msgInfo);
        RequestBody requestBody = FormBody.create(JSONS, JSON.toJSONString(msgInfo));
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = client.newCall(request);
        Response response = null;
        String responseData = null;
        response = call.execute();
        responseData = response.body().string();
        return responseData;

    }

    @Override
    public void close() throws Exception {

    }
}
