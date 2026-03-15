package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TmdbResponse {

    @SerializedName("results")
    private List<MediaItem> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    @SerializedName("page")
    private int page;

    public List<MediaItem> getResults()  { return results; }
    public int getTotalPages()           { return totalPages; }
    public int getTotalResults()         { return totalResults; }
    public int getPage()                 { return page; }
}