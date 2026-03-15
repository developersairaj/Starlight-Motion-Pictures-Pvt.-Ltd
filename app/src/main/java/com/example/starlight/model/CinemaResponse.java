package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CinemaResponse {

    @SerializedName("results")
    private List<CinemaItem> results;

    @SerializedName("total_pages")
    private int totalPages;

    public List<CinemaItem> getResults()    { return results; }
    public int              getTotalPages() { return totalPages; }
}