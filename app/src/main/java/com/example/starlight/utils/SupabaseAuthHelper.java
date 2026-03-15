package com.example.starlight.utils;

import com.example.starlight.api.SupabaseClient;
import com.example.starlight.model.UserModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupabaseAuthHelper {

    private static final String API_KEY = SupabaseConfig.SUPABASE_ANON_KEY;
    private static final String BEARER  = "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY;

    public interface AuthCallback {
        void onSuccess(String name, String email);
        void onError(String message);
    }

    // ── Signup ──────────────────────────────────────────────────
    public static void signup(String name, String email,
                              String password, AuthCallback callback) {
        // First check if email exists
        SupabaseClient.getInstance().getApi()
                .checkEmailExists(API_KEY, BEARER,
                        "eq." + email.toLowerCase().trim(), "email")
                .enqueue(new Callback<List<UserModel>>() {
                    @Override
                    public void onResponse(Call<List<UserModel>> call,
                                           Response<List<UserModel>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {
                            callback.onError("This email is already registered");
                            return;
                        }
                        // Email is free — proceed to insert
                        insertUser(name, email, password, callback);
                    }

                    @Override
                    public void onFailure(Call<List<UserModel>> call, Throwable t) {
                        callback.onError("Network error. Check your connection.");
                    }
                });
    }

    private static void insertUser(String name, String email,
                                   String password, AuthCallback callback) {
        UserModel user = new UserModel(
                name.trim(),
                email.trim().toLowerCase(),
                password);

        SupabaseClient.getInstance().getApi()
                .signupUser(API_KEY, BEARER, user)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call,
                                           Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(name.trim(),
                                    email.trim().toLowerCase());
                        } else {
                            callback.onError("Signup failed. Try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Network error. Check your connection.");
                    }
                });
    }

    // ── Login ───────────────────────────────────────────────────
    public static void login(String email, String password,
                             AuthCallback callback) {
        SupabaseClient.getInstance().getApi()
                .loginUser(API_KEY, BEARER,
                        "eq." + email.trim().toLowerCase(),
                        "eq." + password,
                        "name,email")
                .enqueue(new Callback<List<UserModel>>() {
                    @Override
                    public void onResponse(Call<List<UserModel>> call,
                                           Response<List<UserModel>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {
                            UserModel user = response.body().get(0);
                            callback.onSuccess(user.getName(), user.getEmail());
                        } else {
                            callback.onError("Invalid email or password");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserModel>> call, Throwable t) {
                        callback.onError("Network error. Check your connection.");
                    }
                });
    }
}