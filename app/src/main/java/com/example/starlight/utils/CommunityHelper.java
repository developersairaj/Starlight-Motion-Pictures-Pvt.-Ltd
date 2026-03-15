package com.example.starlight.utils;

import com.example.starlight.api.SupabaseClient;
import com.example.starlight.model.CommunityMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityHelper {

    public interface MessagesCallback {
        void onSuccess(List<CommunityMessage> messages);
        void onError(String error);
    }

    public interface PostCallback {
        void onSuccess();
        void onError(String error);
    }

    private static String apiKey() {
        return SupabaseConfig.SUPABASE_ANON_KEY;
    }

    private static String bearer() {
        return "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY;
    }

    /**
     * Load messages for a community room.
     * Passes all 5 required args to getMessages(apiKey, bearer, room, order, limit).
     */
    public static void getMessages(String room, MessagesCallback cb) {
        SupabaseClient.getInstance()
                .getApi()
                .getMessages(
                        apiKey(),           // arg 1: apikey header
                        bearer(),           // arg 2: Authorization header
                        "eq." + room,       // arg 3: room filter (Supabase syntax)
                        "created_at.desc",  // arg 4: order newest first
                        60)                 // arg 5: limit
                .enqueue(new Callback<List<CommunityMessage>>() {

                    @Override
                    public void onResponse(
                            Call<List<CommunityMessage>> call,
                            Response<List<CommunityMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cb.onSuccess(response.body());
                        } else {
                            cb.onError("Failed to load: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<CommunityMessage>> call,
                            Throwable t) {
                        cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }

    /**
     * Post a new message to the community.
     * Passes all 4 required args to postMessage(apiKey, bearer, contentType, body).
     */
    public static void postMessage(CommunityMessage msg, PostCallback cb) {
        SupabaseClient.getInstance()
                .getApi()
                .postMessage(
                        apiKey(),
                        bearer(),
                        "application/json",
                        msg)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            cb.onSuccess();
                        } else {
                            cb.onError("Send failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }
}