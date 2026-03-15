package com.example.starlight.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.starlight.api.RetrofitClient;
import com.example.starlight.api.JikanApiService;
import com.example.starlight.model.AnimeItem;
import com.example.starlight.model.AnimeResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimeViewModel extends AndroidViewModel {

    private final JikanApiService api;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<AnimeItem>> topAnimeList    = new MutableLiveData<>();
    private final MutableLiveData<List<AnimeItem>> topMangaList    = new MutableLiveData<>();
    private final MutableLiveData<List<AnimeItem>> upcomingList    = new MutableLiveData<>();
    private final MutableLiveData<List<AnimeItem>> airingList      = new MutableLiveData<>();
    private final MutableLiveData<List<AnimeItem>> searchAnimeList = new MutableLiveData<>();
    private final MutableLiveData<List<AnimeItem>> searchMangaList = new MutableLiveData<>();
    private final MutableLiveData<Boolean>         isLoading       = new MutableLiveData<>(false);
    private final MutableLiveData<String>          errorMsg        = new MutableLiveData<>();

    private boolean dataLoaded = false;

    public static final int SUB_TOP_ANIME  = 0;
    public static final int SUB_AIRING     = 1;
    public static final int SUB_UPCOMING   = 2;
    public static final int SUB_TOP_MANGA  = 3;

    public AnimeViewModel(@NonNull Application app) {
        super(app);
        api = RetrofitClient.getInstance().getJikan();
    }

    public LiveData<List<AnimeItem>> getTopAnimeList()    { return topAnimeList; }
    public LiveData<List<AnimeItem>> getTopMangaList()    { return topMangaList; }
    public LiveData<List<AnimeItem>> getUpcomingList()    { return upcomingList; }
    public LiveData<List<AnimeItem>> getAiringList()      { return airingList; }
    public LiveData<List<AnimeItem>> getSearchAnimeList() { return searchAnimeList; }
    public LiveData<List<AnimeItem>> getSearchMangaList() { return searchMangaList; }
    public LiveData<Boolean>         getIsLoading()       { return isLoading; }
    public LiveData<String>          getErrorMsg()        { return errorMsg; }

    public void loadAllAnimeData() {
        // Reuse on rotation
        if (dataLoaded
                && topAnimeList.getValue() != null
                && !topAnimeList.getValue().isEmpty()) {
            return;
        }
        dataLoaded = true;
        isLoading.postValue(true);

        // Jikan has rate limits — large gaps between calls!
        loadTopAnime();
        handler.postDelayed(this::loadAiring,   4000);
        handler.postDelayed(this::loadUpcoming, 8000);
        handler.postDelayed(this::loadTopManga,12000);
    }

    private void loadTopAnime() {
        api.getTopAnime(1, 24)
                .enqueue(new Callback<AnimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnimeResponse> c,
                                           @NonNull Response<AnimeResponse> r) {
                        isLoading.postValue(false);
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getData() != null)
                            topAnimeList.postValue(r.body().getData());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnimeResponse> c,
                                          @NonNull Throwable t) {
                        isLoading.postValue(false);
                        errorMsg.postValue("Failed to load anime!");
                    }
                });
    }

    private void loadAiring() {
        api.getCurrentlyAiring(1)
                .enqueue(new Callback<AnimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnimeResponse> c,
                                           @NonNull Response<AnimeResponse> r) {
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getData() != null)
                            airingList.postValue(r.body().getData());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnimeResponse> c,
                                          @NonNull Throwable t) {}
                });
    }

    private void loadUpcoming() {
        api.getUpcomingAnime(1)
                .enqueue(new Callback<AnimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnimeResponse> c,
                                           @NonNull Response<AnimeResponse> r) {
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getData() != null)
                            upcomingList.postValue(r.body().getData());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnimeResponse> c,
                                          @NonNull Throwable t) {}
                });
    }

    private void loadTopManga() {
        api.getTopManga(1, 24)
                .enqueue(new Callback<AnimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnimeResponse> c,
                                           @NonNull Response<AnimeResponse> r) {
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getData() != null)
                            topMangaList.postValue(r.body().getData());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnimeResponse> c,
                                          @NonNull Throwable t) {}
                });
    }

    public void searchAnime(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchAnimeList.postValue(new ArrayList<>());
            return;
        }
        isLoading.postValue(true);
        api.searchAnime(query.trim(), 20)
                .enqueue(new Callback<AnimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnimeResponse> c,
                                           @NonNull Response<AnimeResponse> r) {
                        isLoading.postValue(false);
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getData() != null)
                            searchAnimeList.postValue(r.body().getData());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnimeResponse> c,
                                          @NonNull Throwable t) {
                        isLoading.postValue(false);
                    }
                });
    }

    public void searchManga(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchMangaList.postValue(new ArrayList<>());
            return;
        }
        api.searchManga(query.trim(), 20)
                .enqueue(new Callback<AnimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AnimeResponse> c,
                                           @NonNull Response<AnimeResponse> r) {
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getData() != null)
                            searchMangaList.postValue(r.body().getData());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnimeResponse> c,
                                          @NonNull Throwable t) {}
                });
    }
}