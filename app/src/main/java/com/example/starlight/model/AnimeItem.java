package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AnimeItem {

    @SerializedName("mal_id")
    private int malId;

    @SerializedName("title")
    private String title;

    @SerializedName("title_english")
    private String titleEnglish;

    @SerializedName("synopsis")
    private String synopsis;

    @SerializedName("score")
    private Double score;

    @SerializedName("scored_by")
    private int scoredBy;

    @SerializedName("rank")
    private int rank;

    @SerializedName("popularity")
    private int popularity;

    @SerializedName("episodes")
    private Integer episodes;

    @SerializedName("chapters")
    private Integer chapters;

    @SerializedName("volumes")
    private Integer volumes;

    @SerializedName("status")
    private String status;

    @SerializedName("airing")
    private boolean airing;

    @SerializedName("publishing")
    private boolean publishing;

    @SerializedName("type")
    private String type;

    @SerializedName("source")
    private String source;

    @SerializedName("duration")
    private String duration;

    @SerializedName("rating")
    private String ageRating;

    @SerializedName("images")
    private AnimeImages images;

    @SerializedName("aired")
    private AiredDates aired;

    @SerializedName("published")
    private AiredDates published;

    @SerializedName("genres")
    private List<AnimeGenre> genres;

    @SerializedName("studios")
    private List<AnimeGenre> studios;

    @SerializedName("authors")
    private List<AnimeGenre> authors;

    @SerializedName("trailer")
    private Trailer trailer;

    // ── Inner classes ─────────────────────────────────────────────────────────

    public static class AnimeImages {
        @SerializedName("jpg")
        public ImageUrls jpg;

        @SerializedName("webp")
        public ImageUrls webp;

        public ImageUrls getJpg()  { return jpg; }
        public ImageUrls getWebp() { return webp; }
    }

    public static class ImageUrls {
        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("small_image_url")
        public String smallImageUrl;

        @SerializedName("large_image_url")
        public String largeImageUrl;

        public String getImageUrl()       { return imageUrl; }
        public String getSmallImageUrl()  { return smallImageUrl; }
        public String getLargeImageUrl()  { return largeImageUrl; }
    }

    public static class AiredDates {
        @SerializedName("from")
        private String from;

        @SerializedName("string")
        private String displayString;

        public String getFrom()          { return from; }
        public String getDisplayString() { return displayString; }
    }

    public static class AnimeGenre {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }

    public static class Trailer {
        @SerializedName("url")
        private String url;
        public String getUrl() { return url; }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int     getMalId()      { return malId; }
    public String  getStatus()     { return status; }
    public boolean isAiring()      { return airing; }
    public boolean isPublishing()  { return publishing; }
    public String  getType()       { return type; }
    public String  getSource()     { return source; }
    public String  getDuration()   { return duration; }
    public String  getAgeRating()  { return ageRating; }
    public int     getRank()       { return rank; }
    public int     getPopularity() { return popularity; }
    public Integer getEpisodes()   { return episodes; }
    public Integer getChapters()   { return chapters; }
    public Integer getVolumes()    { return volumes; }
    public Double  getScore()      { return score; }

    // ── Display helpers ───────────────────────────────────────────────────────

    public String getDisplayTitle() {
        if (titleEnglish != null && !titleEnglish.isEmpty()
                && !"null".equals(titleEnglish))
            return titleEnglish;
        return title != null ? title : "Unknown";
    }

    public String getJapaneseTitle() {
        return title != null ? title : "";
    }

    public String getSynopsis() {
        if (synopsis == null || synopsis.isEmpty())
            return "No synopsis available.";
        return synopsis.length() > 500
                ? synopsis.substring(0, 500) + "\u2026"
                : synopsis;
    }

    /** Poster URL — tries JPG large first, falls back to WebP, skips placeholder images */
    public String getPosterUrl() {
        if (images == null) return null;

        if (images.jpg != null) {
            if (images.jpg.largeImageUrl != null
                    && !images.jpg.largeImageUrl.isEmpty()
                    && !images.jpg.largeImageUrl.contains("questionmark"))
                return images.jpg.largeImageUrl;
            if (images.jpg.imageUrl != null
                    && !images.jpg.imageUrl.isEmpty()
                    && !images.jpg.imageUrl.contains("questionmark"))
                return images.jpg.imageUrl;
        }

        if (images.webp != null) {
            if (images.webp.largeImageUrl != null
                    && !images.webp.largeImageUrl.isEmpty()
                    && !images.webp.largeImageUrl.contains("questionmark"))
                return images.webp.largeImageUrl;
            if (images.webp.imageUrl != null
                    && !images.webp.imageUrl.isEmpty()
                    && !images.webp.imageUrl.contains("questionmark"))
                return images.webp.imageUrl;
        }

        return null;
    }

    public String getFormattedScore() {
        return score != null && score > 0
                ? String.format("%.1f", score) : "N/A";
    }

    /** Short episode/chapter count for list cards */
    public String getEpisodeInfo() {
        if (episodes != null && episodes > 0) return episodes + " eps";
        if (chapters != null && chapters > 0) return chapters + " ch";
        return "Ongoing";
    }

    public String getGenresString() {
        if (genres == null || genres.isEmpty()) return "Anime";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(genres.size(), 3); i++) {
            if (i > 0) sb.append(" \u2022 ");
            sb.append(genres.get(i).getName());
        }
        return sb.toString();
    }

    /** Returns studio names joined by " • " (for detail screen) */
    public String getStudiosString() {
        if (studios == null || studios.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (AnimeGenre s : studios) {
            if (sb.length() > 0) sb.append(" \u2022 ");
            sb.append(s.getName());
        }
        return sb.toString();
    }

    /** Returns author names joined by " • " (for manga detail screen) */
    public String getAuthorsString() {
        if (authors == null || authors.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (AnimeGenre a : authors) {
            if (sb.length() > 0) sb.append(" \u2022 ");
            sb.append(a.getName());
        }
        return sb.toString();
    }

    /** Single studio or author label (for adapter list cards) */
    public String getStudioOrAuthor() {
        if (studios != null && !studios.isEmpty())
            return "\ud83c\udfac " + studios.get(0).getName();
        if (authors != null && !authors.isEmpty())
            return "\u270d\ufe0f " + authors.get(0).getName();
        return "";
    }

    public String getReleaseDate() {
        AiredDates dates = aired != null ? aired : published;
        if (dates == null) return "TBA";
        if (dates.getDisplayString() != null && !dates.getDisplayString().isEmpty())
            return dates.getDisplayString();
        if (dates.getFrom() != null && dates.getFrom().length() >= 10)
            return dates.getFrom().substring(0, 10);
        return "TBA";
    }

    /** Alias for getReleaseDate() used by AnimeDetailActivity */
    public String getAiredString() {
        return getReleaseDate();
    }

    public String getYear() {
        AiredDates dates = aired != null ? aired : published;
        if (dates == null || dates.getFrom() == null) return "";
        return dates.getFrom().length() >= 4
                ? dates.getFrom().substring(0, 4) : "";
    }

    public String getAiringStatus() {
        if (airing || publishing) return "AIRING NOW";
        if ("Not yet aired".equalsIgnoreCase(status)
                || "Upcoming".equalsIgnoreCase(status))
            return "UPCOMING";
        return "FINISHED";
    }

    public boolean isUpcoming() {
        return "Not yet aired".equalsIgnoreCase(status)
                || "Upcoming".equalsIgnoreCase(status);
    }

    public String getTrailerUrl() {
        return trailer != null ? trailer.getUrl() : null;
    }

    public String getStreamingInfo() {
        return "Crunchyroll \u2022 Netflix \u2022 Funimation \u2022 Bilibili";
    }
}