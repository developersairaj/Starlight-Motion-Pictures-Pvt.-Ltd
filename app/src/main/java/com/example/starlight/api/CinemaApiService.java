package com.example.starlight.api;

import com.example.starlight.model.CinemaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CinemaApiService {

    @GET("movie/now_playing")
    Call<CinemaResponse> getNowPlaying(
            @Query("api_key")  String apiKey,
            @Query("language") String language,
            @Query("region")   String region,
            @Query("page")     int    page
    );
}