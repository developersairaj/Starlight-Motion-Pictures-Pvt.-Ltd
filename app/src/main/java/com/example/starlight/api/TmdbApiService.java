package com.example.starlight.api;

import com.example.starlight.model.MediaItem;
import com.example.starlight.model.TmdbResponse;
import com.example.starlight.model.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApiService {

    // ── Trending ──────────────────────────────────────────────────────────────
    @GET("trending/all/day")
    Call<TmdbResponse> getTrending(
            @Query("api_key")  String apiKey,
            @Query("language") String language);

    // ── Movies ────────────────────────────────────────────────────────────────
    @GET("movie/popular")
    Call<TmdbResponse> getPopularMovies(
            @Query("api_key")  String apiKey,
            @Query("language") String language,
            @Query("page")     int page);

    @GET("discover/movie")
    Call<TmdbResponse> getMoviesByLanguage(
            @Query("api_key")                String apiKey,
            @Query("with_original_language") String lang,
            @Query("sort_by")                String sortBy,
            @Query("page")                   int page);

    @GET("discover/movie")
    Call<TmdbResponse> getCartoonMovies(
            @Query("api_key")     String apiKey,
            @Query("with_genres") String genres,
            @Query("sort_by")     String sortBy,
            @Query("page")        int page);

    // ── TV Shows ──────────────────────────────────────────────────────────────
    @GET("tv/popular")
    Call<TmdbResponse> getPopularShows(
            @Query("api_key")  String apiKey,
            @Query("language") String language,
            @Query("page")     int page);

    @GET("discover/tv")
    Call<TmdbResponse> getShowsByLanguage(
            @Query("api_key")                String apiKey,
            @Query("with_original_language") String lang,
            @Query("sort_by")                String sortBy,
            @Query("page")                   int page);

    @GET("discover/tv")
    Call<TmdbResponse> getCartoonShows(
            @Query("api_key")     String apiKey,
            @Query("with_genres") String genres,
            @Query("sort_by")     String sortBy,
            @Query("page")        int page);

    // ── Search ────────────────────────────────────────────────────────────────
    @GET("search/multi")
    Call<TmdbResponse> searchMulti(
            @Query("api_key")  String apiKey,
            @Query("query")    String query,
            @Query("language") String language);

    // ── Details ───────────────────────────────────────────────────────────────
    @GET("movie/{id}")
    Call<MediaItem> getMovieDetails(
            @Path("id")                  int id,
            @Query("api_key")            String apiKey,
            @Query("append_to_response") String append);

    @GET("tv/{id}")
    Call<MediaItem> getTvDetails(
            @Path("id")                  int id,
            @Query("api_key")            String apiKey,
            @Query("append_to_response") String append);

    // ── Videos / Trailers ─────────────────────────────────────────────────────
    @GET("movie/{id}/videos")
    Call<VideoResponse> getMovieVideos(
            @Path("id")       int id,
            @Query("api_key") String apiKey);

    @GET("tv/{id}/videos")
    Call<VideoResponse> getTvVideos(
            @Path("id")       int id,
            @Query("api_key") String apiKey);
}