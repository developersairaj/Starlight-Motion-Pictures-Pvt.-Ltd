package com.example.starlight.api;

import com.example.starlight.model.CommunityMessage;
import com.example.starlight.model.UserModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApiService {

    // ─── AUTH ────────────────────────────────────────────────────────────────

    @POST("/rest/v1/users")
    Call<Void> signupUser(
            @Header("apikey")        String apiKey,
            @Header("Authorization") String bearerToken,
            @Body                    UserModel user
    );

    @GET("/rest/v1/users")
    Call<List<UserModel>> loginUser(
            @Header("apikey")        String apiKey,
            @Header("Authorization") String bearerToken,
            @Query("email")          String emailFilter,
            @Query("password")       String passwordFilter,
            @Query("select")         String select
    );

    @GET("/rest/v1/users")
    Call<List<UserModel>> checkEmailExists(
            @Header("apikey")        String apiKey,
            @Header("Authorization") String bearerToken,
            @Query("email")          String emailFilter,
            @Query("select")         String select
    );

    // ─── COMMUNITY ───────────────────────────────────────────────────────────

    @POST("/rest/v1/community_messages")
    Call<Void> postMessage(
            @Header("apikey")        String apiKey,
            @Header("Authorization") String bearer,
            @Header("Content-Type")  String contentType,
            @Body                    CommunityMessage message
    );

    @GET("/rest/v1/community_messages")
    Call<List<CommunityMessage>> getMessages(
            @Header("apikey")        String apiKey,
            @Header("Authorization") String bearer,
            @Query("room")           String room,
            @Query("order")          String order,
            @Query("limit")          int limit
    );
}