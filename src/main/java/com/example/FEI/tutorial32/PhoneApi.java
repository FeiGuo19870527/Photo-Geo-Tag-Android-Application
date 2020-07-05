package com.example.haotian.tutorial32;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 手机号相关的API
 * Created by Asia on 2016/3/24 0024.
 */
public class PhoneApi {

    /**
     * HOST address
     */
    public static final String BASE_URL = "base_url";
    /**
     *
     */

    /**
     * PhoneApi
     * @return
     */
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    public static PhoneApi getApi(){
        return ApiHolder.phoneApi;
    }

    static class ApiHolder{
        private static PhoneApi phoneApi = new PhoneApi();
    }

    private PhoneService service;

    private PhoneApi(){
        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        service = retrofit.create(PhoneService.class);
    }

    /**
     * 获取PhoneService实例
     * @return
     */
    public PhoneService getService(){
        return service;
    }
}
