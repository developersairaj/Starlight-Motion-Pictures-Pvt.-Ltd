package com.example.starlight.model;

import com.example.starlight.utils.Constants;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MediaItem {

    @SerializedName("id")
    private int id;

    // Movie fields
    @SerializedName("title")
    private String title;

    @SerializedName("release_date")
    private String releaseDate;

    // TV fields
    @SerializedName("name")
    private String name;

    @SerializedName("first_air_date")
    private String firstAirDate;

    @SerializedName("origin_country")
    private List<String> originCountry;

    // Common
    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("vote_count")
    private int voteCount;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("original_language")
    private String originalLanguage;

    @SerializedName("popularity")
    private double popularity;

    // ── Detail-only fields (populated from movie/{id} or tv/{id}) ────────────

    /** Movies: runtime in minutes (e.g. 148) */
    @SerializedName("runtime")
    private int runtime;

    /** TV shows: list of episode run times in minutes (e.g. [45, 60]) */
    @SerializedName("episode_run_time")
    private List<Integer> episodeRunTime;

    /** TV shows: number of seasons */
    @SerializedName("number_of_seasons")
    private int numberOfSeasons;

    /** TV shows: number of episodes */
    @SerializedName("number_of_episodes")
    private int numberOfEpisodes;

    /** TV shows: current status (Returning Series, Ended, etc.) */
    @SerializedName("status")
    private String status;

    /** Genres list with name (from detail endpoint) */
    @SerializedName("genres")
    private List<Genre> genres;

    public static class Genre {
        @SerializedName("id")   private int    id;
        @SerializedName("name") private String name;
        public int    getId()   { return id; }
        public String getName() { return name; }
    }

    // Manually set for classification
    private String resolvedType;

    // ── Basic getters ─────────────────────────────────────────────────────────

    public int    getId()               { return id; }
    public String getMediaType()        { return mediaType; }
    public String getOriginalLanguage() { return originalLanguage; }
    public double getPopularity()       { return popularity; }
    public double getVoteAverage()      { return voteAverage; }
    public int    getVoteCount()        { return voteCount; }
    public List<Integer> getGenreIds()  { return genreIds; }
    public String getResolvedType()     { return resolvedType; }
    public void   setResolvedType(String t) { resolvedType = t; }
    public String getStatus()           { return status; }
    public int    getNumberOfSeasons()  { return numberOfSeasons; }
    public int    getNumberOfEpisodes() { return numberOfEpisodes; }
    public List<Genre> getGenreList()   { return genres; }

    // ── Runtime getters ───────────────────────────────────────────────────────

    public String getFormattedRuntime() {
        return getFormattedRuntimeForType(null);
    }

    public String getFormattedRuntimeForType(String resolvedType) {
        boolean isMovie = resolvedType == null
                ? (runtime > 0 && (episodeRunTime == null || episodeRunTime.isEmpty()))
                : resolvedType.equalsIgnoreCase("MOVIE") || resolvedType.equalsIgnoreCase("movie");

        if (isMovie && runtime > 0) {
            int h = runtime / 60;
            int m = runtime % 60;
            String base;
            if (h > 0 && m > 0) base = h + "h " + m + "m";
            else if (h > 0)     base = h + "h";
            else                 base = m + " min";
            return base;
        }

        int epMin = 0;
        if (episodeRunTime != null && !episodeRunTime.isEmpty()) {
            epMin = episodeRunTime.get(0);
        }

        String epDuration;
        if (epMin >= 60) {
            int h = epMin / 60;
            int m = epMin % 60;
            epDuration = (m > 0) ? h + "h " + m + "m/ep" : h + "h/ep";
        } else if (epMin > 0) {
            epDuration = epMin + " min/ep";
        } else {
            epDuration = null;
        }

        String seasonsStr = null;
        if (numberOfSeasons > 0) {
            seasonsStr = numberOfSeasons + " Season" + (numberOfSeasons > 1 ? "s" : "");
            if (numberOfEpisodes > 0) {
                seasonsStr += "  •  " + numberOfEpisodes + " Ep";
            }
        } else if (numberOfEpisodes > 0) {
            seasonsStr = numberOfEpisodes + " Episodes";
        }

        String totalHint = null;
        if (epMin >= 40 && numberOfEpisodes > 0 && numberOfSeasons > 0) {
            int epPerSeason = numberOfEpisodes / numberOfSeasons;
            if (epPerSeason > 1) {
                int totalMin = epMin * epPerSeason;
                int th = totalMin / 60;
                int tm = totalMin % 60;
                totalHint = "~" + th + "h" + (tm > 0 ? " " + tm + "m" : "") + " / season";
            }
        }

        StringBuilder sb = new StringBuilder();
        if (epDuration != null)  sb.append(epDuration);
        if (seasonsStr != null)  { if (sb.length() > 0) sb.append("  •  "); sb.append(seasonsStr); }
        if (totalHint != null)   { if (sb.length() > 0) sb.append("  •  "); sb.append(totalHint); }

        return sb.length() > 0 ? sb.toString() : null;
    }

    public int getRuntime() { return runtime; }
    public List<Integer> getEpisodeRunTime() { return episodeRunTime; }

    // ── Display helpers ───────────────────────────────────────────────────────

    public String getDisplayTitle() {
        if (title != null && !title.isEmpty()) return title;
        if (name  != null && !name.isEmpty())  return name;
        return "Unknown";
    }

    public String getYear() {
        String date = releaseDate != null ? releaseDate : firstAirDate;
        if (date != null && date.length() >= 4) return date.substring(0, 4);
        return "";
    }

    public String getOverview() {
        return overview != null && !overview.isEmpty()
                ? overview : "No description available.";
    }

    public String getPosterUrl() {
        if (posterPath != null && !posterPath.isEmpty())
            return Constants.TMDB_IMG_BASE + posterPath;
        return null;
    }

    public String getBackdropUrl() {
        if (backdropPath != null && !backdropPath.isEmpty())
            return Constants.TMDB_IMG_ORIG + backdropPath;
        return getPosterUrl();
    }

    public String getFormattedRating() {
        if (voteAverage <= 0) return "N/A";
        return String.format("%.1f", voteAverage);
    }

    public String getGenresString() {
        if (genres != null && !genres.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Genre g : genres) {
                if (sb.length() > 0) sb.append(" • ");
                sb.append(g.getName());
            }
            return sb.toString();
        }
        return "";
    }

    public boolean isAnimation() {
        if (genreIds != null)
            for (int gid : genreIds)
                if (gid == Constants.GENRE_ANIMATION) return true;
        if (genres != null)
            for (Genre g : genres)
                if (g.getId() == Constants.GENRE_ANIMATION) return true;
        return false;
    }

    public boolean isIndianContent() {
        if (originalLanguage == null) return false;
        return originalLanguage.equals(Constants.LANG_HINDI)    ||
                originalLanguage.equals(Constants.LANG_TAMIL)    ||
                originalLanguage.equals(Constants.LANG_TELUGU)   ||
                originalLanguage.equals(Constants.LANG_BENGALI)  ||
                originalLanguage.equals(Constants.LANG_MARATHI)  ||
                originalLanguage.equals(Constants.LANG_MALAYALAM)||
                originalLanguage.equals(Constants.LANG_KANNADA);
    }

    /**
     * Returns a flag emoji representing the original language of this media item.
     * Falls back to 🌐 for unknown/unsupported languages.
     */
    public String getLanguageFlag() {
        if (originalLanguage == null) return "🌐";
        switch (originalLanguage) {
            case "en": return "🇺🇸";
            case "hi": return "🇮🇳";
            case "ta": return "🇮🇳";
            case "te": return "🇮🇳";
            case "bn": return "🇮🇳";
            case "mr": return "🇮🇳";
            case "ml": return "🇮🇳";
            case "kn": return "🇮🇳";
            case "ja": return "🇯🇵";
            case "ko": return "🇰🇷";
            case "zh": return "🇨🇳";
            case "fr": return "🇫🇷";
            case "de": return "🇩🇪";
            case "es": return "🇪🇸";
            case "pt": return "🇧🇷";
            case "it": return "🇮🇹";
            case "ru": return "🇷🇺";
            case "ar": return "🇸🇦";
            case "tr": return "🇹🇷";
            case "th": return "🇹🇭";
            default:   return "🌐";
        }
    }

    /**
     * Returns streaming platform info for display.
     * Extend this with real watch-provider data from the TMDB API if needed.
     */
    public String getStreamingPlatforms() {
        return "";
    }
}