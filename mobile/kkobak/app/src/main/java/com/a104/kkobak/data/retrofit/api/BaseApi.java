package com.a104.kkobak.data.retrofit.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApi {
    protected static final String BASE_URL = "https://kkobak.ml/api/";

    protected static Retrofit getInstance() {
        Gson gson = new GsonBuilder().setLenient().create();
        return (new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build());
    }
}
