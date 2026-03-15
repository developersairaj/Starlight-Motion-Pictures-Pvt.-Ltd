package com.example.starlight;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.starlight.api.RetrofitClient;
import com.example.starlight.model.AnimeDetailResponse;
import com.example.starlight.model.AnimeItem;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimeDetailActivity extends AppCompatActivity {

    private AnimeItem currentAnime;
    private boolean   isManga;

    private ImageView   imgBackdrop;
    private TextView    tvTitle, tvScore, tvGenres,
            tvEpisodes, tvDuration, tvStatus,
            tvStudio, tvRelease, tvSynopsis,
            tvStream, tvSavedReview, tvAgeRating;
    private Button      btnTrailer, btnSubmitReview;
    private RatingBar   ratingBar;
    private EditText    etReview;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        int    id    = getIntent().getIntExtra("anime_id", -1);
        String title = getIntent().getStringExtra("anime_title");
        isManga      = getIntent().getBooleanExtra("is_manga", false);

        if (getSupportActionBar() != null && title != null)
            getSupportActionBar().setTitle(title);

        bindViews();
        setupReview();
        if (id != -1) loadDetails(id);
    }

    private void bindViews() {
        imgBackdrop     = findViewById(R.id.imgAnimeBackdrop);
        tvTitle         = findViewById(R.id.tvAnimeTitle);
        tvScore         = findViewById(R.id.tvAnimeScore);
        tvGenres        = findViewById(R.id.tvAnimeGenres);
        tvEpisodes      = findViewById(R.id.tvAnimeEpisodes);
        tvDuration      = findViewById(R.id.tvAnimeDuration);
        tvStatus        = findViewById(R.id.tvAnimeStatus);
        tvStudio        = findViewById(R.id.tvAnimeStudio);
        tvRelease       = findViewById(R.id.tvAnimeRelease);
        tvSynopsis      = findViewById(R.id.tvAnimeSynopsis);
        tvStream        = findViewById(R.id.tvAnimeStream);
        tvSavedReview   = findViewById(R.id.tvAnimeSavedReview);
        tvAgeRating     = findViewById(R.id.tvAnimeAgeRating);
        btnTrailer      = findViewById(R.id.btnAnimeTrailer);
        btnSubmitReview = findViewById(R.id.btnAnimeSubmitReview);
        ratingBar       = findViewById(R.id.animeRatingBar);
        etReview        = findViewById(R.id.etAnimeReview);
        progress        = findViewById(R.id.progressAnime);
    }

    private void loadDetails(int id) {
        if (progress != null) progress.setVisibility(View.VISIBLE);

        Call<AnimeDetailResponse> call = isManga
                ? RetrofitClient.getInstance().getJikan().getMangaDetails(id)
                : RetrofitClient.getInstance().getJikan().getAnimeDetails(id);

        call.enqueue(new Callback<AnimeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<AnimeDetailResponse> c,
                                   @NonNull Response<AnimeDetailResponse> r) {
                if (progress != null) progress.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null
                        && r.body().getData() != null) {
                    currentAnime = r.body().getData();
                    displayAnime(currentAnime);
                } else {
                    Toast.makeText(AnimeDetailActivity.this,
                            "Could not load details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AnimeDetailResponse> c,
                                  @NonNull Throwable t) {
                if (progress != null) progress.setVisibility(View.GONE);
                Toast.makeText(AnimeDetailActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAnime(AnimeItem a) {
        // Title
        String title = a.getDisplayTitle();
        if (tvTitle != null) tvTitle.setText(title);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);

        // Score
        if (tvScore != null) {
            Double score = a.getScore();
            tvScore.setText(score != null && score > 0
                    ? "\u2b50 " + String.format("%.1f", score) + " / 10"
                    : "\u2b50 N/A");
        }

        // Duration
        if (tvDuration != null) {
            String dur = buildDurationString(a);
            if (dur != null && !dur.isEmpty()) {
                tvDuration.setText("\u23f1 " + dur);
                tvDuration.setVisibility(View.VISIBLE);
            } else {
                tvDuration.setVisibility(View.GONE);
            }
        }

        // Episodes / Chapters
        if (tvEpisodes != null) {
            String epStr = buildEpisodesString(a);
            if (epStr != null && !epStr.isEmpty()) {
                tvEpisodes.setText(epStr);
                tvEpisodes.setVisibility(View.VISIBLE);
            } else {
                tvEpisodes.setVisibility(View.GONE);
            }
        }

        // Status
        if (tvStatus != null) {
            String st = a.getStatus();
            if (st != null && !st.isEmpty()) {
                boolean active = a.isAiring() || a.isPublishing();
                String icon = active ? "\ud83d\udfe2" : "\u26ab";
                tvStatus.setText(icon + " " + st);
                tvStatus.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setVisibility(View.GONE);
            }
        }

        // Age Rating
        if (tvAgeRating != null) {
            String ag = a.getAgeRating();
            if (ag != null && !ag.isEmpty()) {
                tvAgeRating.setText("\ud83d\udd1e " + ag);
                tvAgeRating.setVisibility(View.VISIBLE);
            } else {
                tvAgeRating.setVisibility(View.GONE);
            }
        }

        // Genres
        if (tvGenres != null) {
            String g = a.getGenresString();
            if (g != null && !g.isEmpty()) {
                tvGenres.setText("\ud83c\udfad " + g);
                tvGenres.setVisibility(View.VISIBLE);
            } else {
                tvGenres.setVisibility(View.GONE);
            }
        }

        // Studio / Authors
        if (tvStudio != null) {
            String studio = isManga ? a.getAuthorsString() : a.getStudiosString();
            if (studio != null && !studio.isEmpty()) {
                String label = isManga ? "\u270d\ufe0f " : "\ud83c\udfe2 ";
                tvStudio.setText(label + studio);
                tvStudio.setVisibility(View.VISIBLE);
            } else {
                tvStudio.setVisibility(View.GONE);
            }
        }

        // Release date
        if (tvRelease != null) {
            String rel = a.getAiredString();
            if (rel != null && !rel.isEmpty() && !"TBA".equals(rel)) {
                tvRelease.setText("\ud83d\udcc5 " + rel);
                tvRelease.setVisibility(View.VISIBLE);
            } else {
                tvRelease.setVisibility(View.GONE);
            }
        }

        // Synopsis
        if (tvSynopsis != null) {
            String synopsis = a.getSynopsis();
            tvSynopsis.setText(synopsis != null ? synopsis : "No synopsis available.");
        }

        // Streaming info
        if (tvStream != null) {
            tvStream.setText(
                    "\u25b6 Where to Watch\n\n"
                            + "\ud83d\udcf1 Crunchyroll  \u2022  Netflix\n"
                            + "\ud83d\udcf1 Prime Video  \u2022  Funimation\n"
                            + "\ud83d\udcf1 JioCinema  \u2022  Hotstar\n\n"
                            + "\ud83d\udca1 Search \"" + title + "\" to confirm."
            );
        }

        // Poster
        String posterUrl = a.getPosterUrl();
        if (posterUrl != null && imgBackdrop != null) {
            Glide.with(this)
                    .load(posterUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgBackdrop);
        }

        // Trailer button
        if (btnTrailer != null) {
            String trailerUrl = a.getTrailerUrl();
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                btnTrailer.setVisibility(View.VISIBLE);
                btnTrailer.setOnClickListener(v ->
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(trailerUrl))));
            } else {
                btnTrailer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Builds a human-readable duration string.
     *   Anime series : "24 min/ep"
     *   Anime movie  : "1h 30m"
     *   Manga        : "~5 min/chapter  •  120 chapters"
     */
    private String buildDurationString(AnimeItem a) {
        String raw = a.getDuration();
        if (raw != null && !raw.isEmpty()
                && !raw.equalsIgnoreCase("unknown")
                && !raw.equalsIgnoreCase("N/A")) {

            // "24 min per ep" -> "24 min/ep", "1 hr 30 min" -> "1h 30min"
            String cleaned = raw
                    .replace(" per ep", "/ep")
                    .replace(" hr ", "h ")
                    .replace("hr", "h")
                    .trim();

            // Movie type — no "/ep" suffix needed
            String animeType = a.getType();
            if (animeType != null && animeType.equalsIgnoreCase("Movie")) {
                cleaned = cleaned.replace("/ep", "").trim();
            }

            return cleaned;
        }

        // Manga fallback — estimated chapter read time
        if (isManga) {
            Integer chapters = a.getChapters();
            if (chapters != null && chapters > 0)
                return "~5 min/chapter  \u2022  " + chapters + " chapters";
        }

        return null;
    }

    /**
     * Builds episodes/chapters/volumes string.
     *   Anime: "📺 24 Episodes"
     *   Manga: "📖 120 Chapters  •  13 Volumes"
     */
    private String buildEpisodesString(AnimeItem a) {
        if (isManga) {
            Integer ch  = a.getChapters();
            Integer vol = a.getVolumes();
            if (ch != null && ch > 0 && vol != null && vol > 0)
                return "\ud83d\udcd6 " + ch + " Chapters  \u2022  " + vol + " Volumes";
            if (ch != null && ch > 0)
                return "\ud83d\udcd6 " + ch + " Chapters";
            if (vol != null && vol > 0)
                return "\ud83d\udcd6 " + vol + " Volumes";
            return "\ud83d\udcd6 Ongoing";
        } else {
            Integer ep = a.getEpisodes();
            if (ep != null && ep > 0)
                return "\ud83d\udcfa " + ep + " Episodes";
            if (a.isAiring())
                return "\ud83d\udcfa Currently Airing";
            return null;
        }
    }

    // ── Review ────────────────────────────────────────────────────────────────

    private void setupReview() {
        if (btnSubmitReview == null) return;
        btnSubmitReview.setOnClickListener(v -> {
            if (currentAnime == null) return;
            float stars = ratingBar != null ? ratingBar.getRating() : 0;
            String text = etReview != null
                    ? etReview.getText().toString().trim() : "";
            if (stars == 0 && text.isEmpty()) {
                Toast.makeText(this, "Add a rating or review first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String review = "\u2b50 " + stars + "/5\n" + text;
            if (tvSavedReview != null) {
                tvSavedReview.setText(review);
                tvSavedReview.setVisibility(View.VISIBLE);
            }
            Toast.makeText(this, "Review saved!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}