package com.example.starlight;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.starlight.api.OmdbApiService;
import com.example.starlight.api.RetrofitClient;
import com.example.starlight.api.TmdbApiService;
import com.example.starlight.model.MediaItem;
import com.example.starlight.model.OmdbResponse;
import com.example.starlight.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    // ── Views — IDs match activity_detail.xml exactly ─────────────────────────
    private ImageView   imgBackdrop;
    private TextView    tvDetailTitle;   // title
    private TextView    tvDetailYear;    // year + type badge (top-right)
    private TextView    tvTmdbRating;    // TMDB star rating
    private TextView    tvImdbRating;    // IMDb rating chip
    private TextView    tvDetailMeta;    // runtime • genres • seasons line
    private TextView    tvDirector;      // director line
    private TextView    tvActors;        // cast line
    private TextView    tvStreaming;     // streaming platforms
    private TextView    tvOverview;      // synopsis
    private ProgressBar progressDetail;  // loading spinner

    private TmdbApiService tmdb;
    private OmdbApiService omdb;

    private int    mediaId;
    private String mediaType;
    private String mediaName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        mediaId   = getIntent().getIntExtra(Constants.EXTRA_MEDIA_ID, -1);
        mediaType = getIntent().getStringExtra(Constants.EXTRA_MEDIA_TYPE);
        mediaName = getIntent().getStringExtra(Constants.EXTRA_MEDIA_NAME);

        bindViews();
        setupApis();

        if (mediaId != -1) {
            loadTmdbDetails();
        } else {
            Toast.makeText(this, "Could not load details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        imgBackdrop   = findViewById(R.id.imgBackdrop);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailYear  = findViewById(R.id.tvDetailYear);
        tvTmdbRating  = findViewById(R.id.tvTmdbRating);
        tvImdbRating  = findViewById(R.id.tvImdbRating);
        tvDetailMeta  = findViewById(R.id.tvDetailMeta);
        tvDirector    = findViewById(R.id.tvDirector);
        tvActors      = findViewById(R.id.tvActors);
        tvStreaming   = findViewById(R.id.tvStreaming);
        tvOverview    = findViewById(R.id.tvOverview);
        progressDetail = findViewById(R.id.progressDetail);
    }

    private void setupApis() {
        tmdb = RetrofitClient.getInstance().getTmdb();
        omdb = RetrofitClient.getInstance().getOmdb();
    }

    // ── Step 1: Load from TMDB ────────────────────────────────────────────────

    private void loadTmdbDetails() {
        if (progressDetail != null) progressDetail.setVisibility(View.VISIBLE);

        boolean isMovie = isMovieType(mediaType);

        Call<MediaItem> call = isMovie
                ? tmdb.getMovieDetails(mediaId, Constants.TMDB_API_KEY, "credits")
                : tmdb.getTvDetails(mediaId, Constants.TMDB_API_KEY, "credits");

        call.enqueue(new Callback<MediaItem>() {
            @Override
            public void onResponse(@NonNull Call<MediaItem> c,
                                   @NonNull Response<MediaItem> r) {
                if (progressDetail != null) progressDetail.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    displayMedia(r.body(), isMovie);
                    // Step 2: OMDB for IMDb rating, director, cast
                    // Pass exact title + year to avoid wrong-movie matches
                    loadOmdbDetails(r.body().getDisplayTitle(), r.body().getYear());
                } else {
                    showFallback();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MediaItem> c, @NonNull Throwable t) {
                if (progressDetail != null) progressDetail.setVisibility(View.GONE);
                showFallback();
            }
        });
    }

    private void displayMedia(MediaItem item, boolean isMovie) {
        String displayTitle = item.getDisplayTitle();

        // Title
        if (tvDetailTitle != null) tvDetailTitle.setText(displayTitle);
        setTitle(displayTitle);

        // Year + type badge
        String year      = item.getYear();
        String typeBadge = isMovie ? "MOVIE" : resolveShowType(mediaType);
        if (tvDetailYear != null)
            tvDetailYear.setText(year.isEmpty() ? typeBadge : year + "  \u2022  " + typeBadge);

        // TMDB star rating
        if (tvTmdbRating != null)
            tvTmdbRating.setText(item.getFormattedRating());

        // Meta line: runtime • genres • seasons/status
        // Packed into tvDetailMeta since the layout uses a single meta TextView
        StringBuilder meta = new StringBuilder();

        // Runtime (smart, type-aware)
        String runtime = item.getFormattedRuntimeForType(mediaType);
        if (runtime != null && !runtime.isEmpty())
            meta.append("\u23f1 ").append(runtime);

        // Genres
        String genres = item.getGenresString();
        if (genres != null && !genres.isEmpty()) {
            if (meta.length() > 0) meta.append("  \u2022  ");
            meta.append(genres);
        }

        // Language
        String lang = item.getOriginalLanguage();
        if (lang != null && !lang.isEmpty()) {
            if (meta.length() > 0) meta.append("  \u2022  ");
            meta.append("\ud83c\udf10 ").append(languageToName(lang));
        }

        // Seasons / episodes for TV
        if (!isMovie) {
            int seasons  = item.getNumberOfSeasons();
            int episodes = item.getNumberOfEpisodes();
            if (seasons > 0) {
                if (meta.length() > 0) meta.append("\n");
                meta.append("\ud83d\udcfa ").append(seasons)
                        .append(" Season").append(seasons > 1 ? "s" : "");
                if (episodes > 0)
                    meta.append("  \u2022  ").append(episodes).append(" Episodes");
            }
            // Status
            String status = item.getStatus();
            if (status != null && !status.isEmpty()) {
                String icon = status.equalsIgnoreCase("Returning Series") ? "\ud83d\udfe2"
                        : status.equalsIgnoreCase("Ended") ? "\u26ab" : "\ud83d\udd35";
                if (meta.length() > 0) meta.append("  \u2022  ");
                meta.append(icon).append(" ").append(status);
            }
        }

        if (tvDetailMeta != null && meta.length() > 0) {
            tvDetailMeta.setText(meta.toString());
            tvDetailMeta.setVisibility(View.VISIBLE);
        }

        // Overview
        if (tvOverview != null)
            tvOverview.setText(item.getOverview());

        // Streaming info
        if (tvStreaming != null) {
            tvStreaming.setText(
                    "Netflix  \u2022  Prime Video  \u2022  JioCinema  \u2022  Hotstar  \u2022  SonyLIV"
            );
        }

        // Backdrop / poster
        String backdrop = item.getBackdropUrl();
        if (backdrop != null && imgBackdrop != null) {
            Glide.with(this)
                    .load(backdrop)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgBackdrop);
        }
    }

    // ── Step 2: OMDB for IMDb rating, director, cast ──────────────────────────

    private void loadOmdbDetails(String title, String year) {
        if (omdb == null || title == null) return;

        omdb.getByTitle(Constants.OMDB_API_KEY, title, year)
                .enqueue(new Callback<OmdbResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<OmdbResponse> c,
                                           @NonNull Response<OmdbResponse> r) {
                        if (!r.isSuccessful() || r.body() == null) return;
                        OmdbResponse d = r.body();
                        if (!d.isSuccess()) return;
                        // OmdbResponse has no getTitle() — use the tmdb title we passed in
                        // to do the match check
                        runOnUiThread(() -> applyOmdbData(d, title));
                    }

                    @Override
                    public void onFailure(@NonNull Call<OmdbResponse> c, @NonNull Throwable t) {
                        // Silently fail — TMDB data already shown
                    }
                });
    }

    private void applyOmdbData(OmdbResponse d, String expectedTitle) {
        // IMDb rating chip
        if (tvImdbRating != null) {
            String imdb = d.getImdbRating();
            if (imdb != null && !imdb.equals("N/A") && !imdb.isEmpty()) {
                tvImdbRating.setText("IMDb " + imdb);
                tvImdbRating.setVisibility(View.VISIBLE);
            }
        }

        // Runtime from OMDB — only use if tvDetailMeta has no runtime yet
        // (append to meta if TMDB gave nothing)
        String rt = d.getRuntime();
        if (rt != null && !rt.equals("N/A") && !rt.isEmpty()
                && tvDetailMeta != null) {
            CharSequence existing = tvDetailMeta.getText();
            if (existing == null || !existing.toString().contains("\u23f1")) {
                String formatted = formatOmdbRuntime(rt);
                if (!isMovieType(mediaType)) formatted = formatted + "/ep";
                String newMeta = "\u23f1 " + formatted
                        + (existing != null && existing.length() > 0
                        ? "  \u2022  " + existing : "");
                tvDetailMeta.setText(newMeta);
                tvDetailMeta.setVisibility(View.VISIBLE);
            }
        }

        // Director
        if (tvDirector != null) {
            String dir = d.getDirector();
            if (dir != null && !dir.equals("N/A") && !dir.isEmpty()) {
                tvDirector.setText("\ud83c\udfac " + dir);
                tvDirector.setVisibility(View.VISIBLE);
            }
        }

        // Cast
        if (tvActors != null) {
            String actors = d.getActors();
            if (actors != null && !actors.equals("N/A") && !actors.isEmpty()) {
                tvActors.setText("\ud83c\udfad " + actors);
                tvActors.setVisibility(View.VISIBLE);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showFallback() {
        if (tvDetailTitle != null && mediaName != null) tvDetailTitle.setText(mediaName);
        Toast.makeText(this, "Could not load full details", Toast.LENGTH_SHORT).show();
    }

    private boolean isMovieType(String type) {
        if (type == null) return false;
        String t = type.toLowerCase();
        return t.equals("movie") || t.equals("movies");
    }

    private String resolveShowType(String type) {
        if (type == null) return "SERIES";
        switch (type.toUpperCase()) {
            case "K-DRAMA": case "KDRAMA": return "K-DRAMA";
            case "C-DRAMA": case "CDRAMA": return "C-DRAMA";
            case "CARTOON": case "ANIMATION": return "CARTOON";
            case "DRAMA": return "DRAMA";
            default: return "SERIES";
        }
    }

    private String languageToName(String code) {
        switch (code) {
            case "en": return "English";
            case "hi": return "Hindi";
            case "ta": return "Tamil";
            case "te": return "Telugu";
            case "ko": return "Korean";
            case "zh": return "Chinese";
            case "ja": return "Japanese";
            case "fr": return "French";
            case "es": return "Spanish";
            case "de": return "German";
            case "ml": return "Malayalam";
            case "bn": return "Bengali";
            case "mr": return "Marathi";
            default:   return code.toUpperCase();
        }
    }

    /** Converts "148 min" to "2h 28m" */
    private String formatOmdbRuntime(String raw) {
        try {
            String numStr = raw.replaceAll("[^0-9]", "").trim();
            if (!numStr.isEmpty()) {
                int mins = Integer.parseInt(numStr);
                if (mins > 0) {
                    int h = mins / 60;
                    int m = mins % 60;
                    if (h > 0 && m > 0) return h + "h " + m + "m";
                    if (h > 0) return h + "h";
                    return m + " min";
                }
            }
        } catch (NumberFormatException ignored) {}
        return raw;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}