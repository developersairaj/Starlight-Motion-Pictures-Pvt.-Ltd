package com.example.starlight;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.starlight.adapter.CinemaAdapter;
import com.example.starlight.api.CinemaApiService;
import com.example.starlight.model.CinemaItem;
import com.example.starlight.model.CinemaResponse;
import com.example.starlight.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CinemaFragment extends Fragment {

    // ── Cities ────────────────────────────────────────────────────────────────
    private static final String[][] CITIES = {
            {"Mumbai",    "mumbai"},
            {"Delhi",     "delhi-ncr"},
            {"Bangalore", "bangalore"},
            {"Hyderabad", "hyderabad"},
            {"Chennai",   "chennai"},
            {"Pune",      "pune"},
            {"Kolkata",   "kolkata"},
            {"Ahmedabad", "ahmedabad"},
            {"Jaipur",    "jaipur"},
            {"Surat",     "surat"},
    };

    private String selectedCitySlug = "mumbai";

    // ── Views ─────────────────────────────────────────────────────────────────
    private LinearLayout  layoutCityChips;
    private RecyclerView  rvCinemaMovies;
    private ProgressBar   progressCinemaCenter;
    private View          layoutCinemaEmpty;
    private TextView      tvCinemaError;
    private View          btnOpenBms;
    private View          btnCinemaRetry;

    // ── State ─────────────────────────────────────────────────────────────────
    private CinemaAdapter    adapter;
    private CinemaApiService api;
    private TextView[]       chipViews;
    private boolean          loaded = false; // only load once per fragment lifetime

    // ─────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cinema, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Bind views ────────────────────────────────────────────────────────
        layoutCityChips      = view.findViewById(R.id.layoutCityChips);
        rvCinemaMovies       = view.findViewById(R.id.rvCinemaMovies);
        progressCinemaCenter = view.findViewById(R.id.progressCinemaCenter);
        layoutCinemaEmpty    = view.findViewById(R.id.layoutCinemaEmpty);
        tvCinemaError        = view.findViewById(R.id.tvCinemaError);
        btnOpenBms           = view.findViewById(R.id.btnOpenBms);
        btnCinemaRetry       = view.findViewById(R.id.btnCinemaRetry);

        // ── Retrofit ──────────────────────────────────────────────────────────
        api = new Retrofit.Builder()
                .baseUrl(Constants.TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CinemaApiService.class);

        // ── RecyclerView ──────────────────────────────────────────────────────
        adapter = new CinemaAdapter(requireContext());
        rvCinemaMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCinemaMovies.setAdapter(adapter);
        rvCinemaMovies.setHasFixedSize(false);

        // Tapping a movie → open BMS search for that movie
        adapter.setOnBookClickListener(movie -> openUrl(movie.getBmsSearchUrl()));

        // ── BMS header button ─────────────────────────────────────────────────
        if (btnOpenBms != null) {
            btnOpenBms.setOnClickListener(v ->
                    openUrl(CinemaItem.getBmsCityUrl(selectedCitySlug)));
        }

        // ── Retry button ──────────────────────────────────────────────────────
        if (btnCinemaRetry != null) {
            btnCinemaRetry.setOnClickListener(v -> {
                loaded = false;
                loadNowPlaying();
            });
        }

        // ── City chips ────────────────────────────────────────────────────────
        buildCityChips();

        // ── Load movies (only on first view creation) ─────────────────────────
        if (!loaded) {
            loadNowPlaying();
        }
    }

    // ── City chips ────────────────────────────────────────────────────────────

    private void buildCityChips() {
        if (layoutCityChips == null) return;
        layoutCityChips.removeAllViews();
        chipViews = new TextView[CITIES.length];

        for (int i = 0; i < CITIES.length; i++) {
            final String display = CITIES[i][0];
            final String slug    = CITIES[i][1];
            final int    idx     = i;

            TextView chip = new TextView(requireContext());
            chip.setText(display);
            chip.setTextSize(11f);
            chip.setSingleLine(true);
            chip.setPadding(dp(12), dp(6), dp(12), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);
            chip.setClickable(true);
            chip.setFocusable(true);

            applyChipStyle(chip, slug.equals(selectedCitySlug));

            chip.setOnClickListener(v -> {
                selectedCitySlug = slug;
                for (int j = 0; j < chipViews.length; j++) {
                    if (chipViews[j] != null)
                        applyChipStyle(chipViews[j], CITIES[j][1].equals(selectedCitySlug));
                }
            });

            chipViews[i] = chip;
            layoutCityChips.addView(chip);
        }
    }

    private void applyChipStyle(TextView chip, boolean selected) {
        // Use setBackgroundResource — never deprecated, never null
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_btn_gold);
            chip.setTextColor(Color.BLACK);
        } else {
            chip.setBackgroundResource(R.drawable.bg_badge_genre);
            chip.setTextColor(Color.parseColor("#C0B8E0"));
        }
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }

    // ── Load movies ───────────────────────────────────────────────────────────

    private void loadNowPlaying() {
        setState(State.LOADING);

        api.getNowPlaying(Constants.TMDB_API_KEY, "en-IN", "IN", 1)
                .enqueue(new Callback<CinemaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CinemaResponse> call,
                                           @NonNull Response<CinemaResponse> resp) {
                        if (!isAdded()) return;

                        if (resp.isSuccessful()
                                && resp.body() != null
                                && resp.body().getResults() != null
                                && !resp.body().getResults().isEmpty()) {

                            List<CinemaItem> movies = resp.body().getResults();
                            loaded = true;
                            requireActivity().runOnUiThread(() -> {
                                if (!isAdded()) return;
                                adapter.setMovies(movies);
                                setState(State.LIST);
                            });

                        } else {
                            requireActivity().runOnUiThread(() -> {
                                if (!isAdded()) return;
                                setError("No movies found right now.\nTry again later.");
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CinemaResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            setError("Network error.\nCheck your connection and try again.");
                        });
                    }
                });
    }

    // ── State machine: LOADING / LIST / EMPTY ─────────────────────────────────

    private enum State { LOADING, LIST, EMPTY }

    private void setState(State state) {
        switch (state) {
            case LOADING:
                progressCinemaCenter.setVisibility(View.VISIBLE);
                rvCinemaMovies.setVisibility(View.GONE);
                layoutCinemaEmpty.setVisibility(View.GONE);
                break;
            case LIST:
                progressCinemaCenter.setVisibility(View.GONE);
                rvCinemaMovies.setVisibility(View.VISIBLE);
                layoutCinemaEmpty.setVisibility(View.GONE);
                break;
            case EMPTY:
                progressCinemaCenter.setVisibility(View.GONE);
                rvCinemaMovies.setVisibility(View.GONE);
                layoutCinemaEmpty.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setError(String message) {
        if (tvCinemaError != null) tvCinemaError.setText(message);
        setState(State.EMPTY);
    }

    // ── URL opener ────────────────────────────────────────────────────────────

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Could not open BookMyShow", Toast.LENGTH_SHORT).show();
        }
    }
}