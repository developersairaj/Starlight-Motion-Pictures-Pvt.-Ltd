package com.example.starlight.api;

import com.example.starlight.model.AnimeDetailResponse;
import com.example.starlight.model.AnimeItem;
import com.example.starlight.model.AnimeResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JikanApiService {

    // ── Top Anime ─────────────────────────────────────────────
    @GET("top/anime")
    Call<AnimeResponse> getTopAnime(
            @Query("page") int page,
            @Query("limit") int limit);

    // ── Top Manga ─────────────────────────────────────────────
    @GET("top/manga")
    Call<AnimeResponse> getTopManga(
            @Query("page") int page,
            @Query("limit") int limit);

    // ── Upcoming Anime ────────────────────────────────────────
    @GET("seasons/upcoming")
    Call<AnimeResponse> getUpcomingAnime(
            @Query("page") int page);

    // ── Currently Airing ──────────────────────────────────────
    @GET("seasons/now")
    Call<AnimeResponse> getCurrentlyAiring(
            @Query("page") int page);

    // ── Search Anime ──────────────────────────────────────────
    @GET("anime")
    Call<AnimeResponse> searchAnime(
            @Query("q") String query,
            @Query("limit") int limit);

    // ── Search Manga ──────────────────────────────────────────
    @GET("manga")
    Call<AnimeResponse> searchManga(
            @Query("q") String query,
            @Query("limit") int limit);

    // ── Anime Details ─────────────────────────────────────────
    @GET("anime/{id}/full")
    Call<AnimeDetailResponse> getAnimeDetails(
            @Path("id") int id);

    // ── Manga Details ─────────────────────────────────────────
    @GET("manga/{id}/full")
    Call<AnimeDetailResponse> getMangaDetails(
            @Path("id") int id);

    // ── Anime by genre ────────────────────────────────────────
    @GET("anime")
    Call<AnimeResponse> getAnimeByGenre(
            @Query("genres") String genreId,
            @Query("order_by") String orderBy,
            @Query("limit") int limit);
}