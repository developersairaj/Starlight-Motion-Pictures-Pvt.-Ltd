package com.example.starlight.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starlight.api.TmdbApiService;
import com.example.starlight.model.MediaItem;
import com.example.starlight.model.TmdbResponse;
import com.example.starlight.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    // ─── Tab constants ───────────────────────────────────────────────────────
    public static final int TAB_TRENDING = 0;
    public static final int TAB_MOVIES   = 1;
    public static final int TAB_SERIES   = 2;
    public static final int TAB_DRAMA    = 3;
    public static final int TAB_KDRAMA   = 4;
    public static final int TAB_CDRAMA   = 5;
    public static final int TAB_CARTOONS = 6;

    // ─── LiveData ────────────────────────────────────────────────────────────
    private final MutableLiveData<List<MediaItem>> mediaList = new MutableLiveData<>();
    private final MutableLiveData<Boolean>         isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String>          errorMsg  = new MutableLiveData<>();

    public LiveData<List<MediaItem>> getMediaList() { return mediaList; }
    public LiveData<Boolean>         getIsLoading()  { return isLoading; }
    public LiveData<String>          getErrorMsg()   { return errorMsg;  }

    // ─── Internals ───────────────────────────────────────────────────────────
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final TmdbApiService  tmdbApi;

    public MainViewModel() {
        tmdbApi = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbApiService.class);
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) return;
        final String q = query.trim();
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                Response<TmdbResponse> r = tmdbApi
                        .searchMulti(Constants.TMDB_API_KEY, q, "en-US")
                        .execute();
                isLoading.postValue(false);
                if (r.isSuccessful() && r.body() != null) {
                    List<MediaItem> raw = r.body().getResults();
                    mediaList.postValue(rankResults(raw, q));
                } else {
                    errorMsg.postValue("Search failed (" + r.code() + ")");
                }
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    private List<MediaItem> rankResults(List<MediaItem> raw, String query) {
        if (raw == null) return new ArrayList<>();
        String q = query.toLowerCase().trim();
        List<MediaItem> exact      = new ArrayList<>();
        List<MediaItem> startsWith = new ArrayList<>();
        List<MediaItem> contains   = new ArrayList<>();
        List<MediaItem> rest       = new ArrayList<>();
        for (MediaItem item : raw) {
            if (item.getResolvedType() == null || item.getResolvedType().isEmpty()) {
                String mt = item.getMediaType();
                item.setResolvedType(mt != null ? mt.toUpperCase() : "MOVIE");
            }
            String title = item.getDisplayTitle();
            if (title == null) { rest.add(item); continue; }
            String t = title.toLowerCase().trim();
            if (t.equals(q)) {
                exact.add(item);
            } else if (t.startsWith(q) || q.startsWith(t)) {
                startsWith.add(item);
            } else if (t.contains(q) || q.contains(t)) {
                contains.add(item);
            } else {
                rest.add(item);
            }
        }
        List<MediaItem> result = new ArrayList<>();
        result.addAll(exact);
        result.addAll(startsWith);
        result.addAll(contains);
        result.addAll(rest);
        return result;
    }

    // ─── Public tab loader ───────────────────────────────────────────────────

    public void loadTab(int tab) {
        switch (tab) {
            case TAB_TRENDING: loadTrending();  break;
            case TAB_MOVIES:   loadMovies();    break;
            case TAB_SERIES:   loadSeries();    break;
            case TAB_DRAMA:    loadDrama();     break;
            case TAB_KDRAMA:   loadKDrama();    break;
            case TAB_CDRAMA:   loadCDrama();    break;
            case TAB_CARTOONS: loadCartoons();  break;
            default:           loadTrending();  break;
        }
    }

    // ─── Tab loaders — use REAL TmdbApiService method signatures ────────────

    /** 🔥 Trending — getTrending(apiKey, language) — 2 args */
    private void loadTrending() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                Response<TmdbResponse> r = tmdbApi
                        .getTrending(Constants.TMDB_API_KEY, "en-US")
                        .execute();
                handleResponse(r, "MOVIE");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    /** 🎬 Movies — getPopularMovies(apiKey, language, page) — 3 args */
    private void loadMovies() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                Response<TmdbResponse> r = tmdbApi
                        .getPopularMovies(Constants.TMDB_API_KEY, "en-US", 1)
                        .execute();
                handleResponse(r, "MOVIE");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    /** 📺 Series — getPopularShows(apiKey, language, page) — 3 args */
    private void loadSeries() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                Response<TmdbResponse> r = tmdbApi
                        .getPopularShows(Constants.TMDB_API_KEY, "en-US", 1)
                        .execute();
                handleResponse(r, "SERIES");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    /** 🎭 Drama — discover TV shows with Drama genre (id=18) */
    private void loadDrama() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // getCartoonShows reused for genre-based discover
                // Signature: getCartoonShows(apiKey, withGenres, sortBy, page)
                Response<TmdbResponse> r = tmdbApi
                        .getCartoonShows(
                                Constants.TMDB_API_KEY,
                                "18",              // Drama genre id
                                "popularity.desc",
                                1)
                        .execute();
                handleResponse(r, "DRAMA");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    /** 🇰🇷 K-Drama — getShowsByLanguage(apiKey, lang, sortBy, page) */
    private void loadKDrama() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                Response<TmdbResponse> r = tmdbApi
                        .getShowsByLanguage(
                                Constants.TMDB_API_KEY,
                                "ko",              // Korean
                                "popularity.desc",
                                1)
                        .execute();
                handleResponse(r, "K-DRAMA");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    /** 🇨🇳 C-Drama — getShowsByLanguage(apiKey, lang, sortBy, page) */
    private void loadCDrama() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                Response<TmdbResponse> r = tmdbApi
                        .getShowsByLanguage(
                                Constants.TMDB_API_KEY,
                                "zh",              // Chinese
                                "popularity.desc",
                                1)
                        .execute();
                handleResponse(r, "C-DRAMA");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    /** 🎨 Cartoons — getCartoonShows(apiKey, withGenres, sortBy, page) */
    private void loadCartoons() {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Animation genre id = 16; English-language filter via language
                Response<TmdbResponse> r = tmdbApi
                        .getCartoonShows(
                                Constants.TMDB_API_KEY,
                                "16",              // Animation genre
                                "popularity.desc",
                                1)
                        .execute();
                handleResponse(r, "CARTOON");
            } catch (Exception e) {
                postError(e);
            }
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void handleResponse(Response<TmdbResponse> response, String type) {
        isLoading.postValue(false);
        if (response.isSuccessful() && response.body() != null) {
            List<MediaItem> raw = response.body().getResults();
            mediaList.postValue(tagItems(raw, type));
        } else {
            errorMsg.postValue("Failed to load (" + response.code() + ")");
        }
    }

    private void postError(Exception e) {
        isLoading.postValue(false);
        Log.e(TAG, "API error", e);
        errorMsg.postValue("Network error — check your connection");
    }

    /**
     * Tag each item with a resolved type so the UI can show the right badge.
     * Uses setResolvedType() — the real setter on MediaItem.
     */
    private List<MediaItem> tagItems(List<MediaItem> raw, String type) {
        if (raw == null) return new ArrayList<>();
        for (MediaItem item : raw) {
            item.setResolvedType(type);
        }
        return raw;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}