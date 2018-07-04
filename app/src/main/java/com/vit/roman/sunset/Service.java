package com.vit.roman.sunset;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Service {
    @GET("/json?")
    Call<JsonObject> getSunset (@Query("lat") float lat,
                                     @Query("lng") float lng);
}
