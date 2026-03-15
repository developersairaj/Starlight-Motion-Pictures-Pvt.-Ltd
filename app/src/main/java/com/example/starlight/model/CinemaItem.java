package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;

public class CinemaItem {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("overview")
    private String overview;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("original_language")
    private String originalLanguage;

    public int getId() { return id; }

    public String getTitle() {
        return (title != null && !title.isEmpty()) ? title : "Unknown";
    }

    public String getOverview() {
        return overview != null ? overview : "";
    }

    public String getOriginalLanguage() {
        return originalLanguage != null ? originalLanguage.toUpperCase() : "";
    }

    public String getPosterUrl() {
        return posterPath != null
                ? "https://image.tmdb.org/t/p/w342" + posterPath
                : null;
    }

    public String getFormattedRating() {
        return voteAverage > 0 ? String.format("%.1f", voteAverage) : "N/A";
    }

    public String getYear() {
        return (releaseDate != null && releaseDate.length() >= 4)
                ? releaseDate.substring(0, 4) : "";
    }

    /** BMS search URL — tapping Book Tickets searches this movie by name */
    public String getBmsSearchUrl() {
        return "https://in.bookmyshow.com/search?q="
                + getTitle().trim().replaceAll("\\s+", "+");
    }

    /** BMS city explore page */
    public static String getBmsCityUrl(String citySlug) {
        return "https://in.bookmyshow.com/explore/movies-" + citySlug;
    }
}