package com.example.starlight.api;

import com.example.starlight.model.OmdbResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbApiService {

    @GET("/")
    Call<OmdbResponse> getByTitle(
            @Query("apikey") String apiKey,
            @Query("t") String title,
            @Query("y") String year);
}