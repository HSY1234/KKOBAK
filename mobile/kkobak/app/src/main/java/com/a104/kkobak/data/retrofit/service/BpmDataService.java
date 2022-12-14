package com.a104.kkobak.data.retrofit.service;

import com.a104.kkobak.data.retrofit.model.BpmDataReq;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface BpmDataService {
    @POST("bpm/add")
    Call<Boolean> sendBpmData(@Header("Authorization") String authorization, @Body BpmDataReq BpmDataReq);
}
