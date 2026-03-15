package com.example.starlight.api;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static volatile RetrofitClient instance;
    private final TmdbApiService  tmdbApi;
    private final OmdbApiService  omdbApi;
    private final JikanApiService jikanApi;

    private RetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(
                        3, 30, TimeUnit.SECONDS))
                .retryOnConnectionFailure(false)
                .build();

        tmdbApi = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbApiService.class);

        omdbApi = new Retrofit.Builder()
                .baseUrl("https://www.omdbapi.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OmdbApiService.class);

        jikanApi = new Retrofit.Builder()
                .baseUrl("https://api.jikan.moe/v4/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JikanApiService.class);
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null)
                    instance = new RetrofitClient();
            }
        }
        return instance;
    }

    public TmdbApiService  getTmdb()  { return tmdbApi;  }
    public OmdbApiService  getOmdb()  { return omdbApi;  }
    public JikanApiService getJikan() { return jikanApi; }
}