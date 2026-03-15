package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AnimeResponse {

    @SerializedName("data")
    private List<AnimeItem> data;

    @SerializedName("pagination")
    private Pagination pagination;

    public List<AnimeItem> getData() { return data; }
    public Pagination getPagination() { return pagination; }

    public static class Pagination {
        @SerializedName("last_visible_page")
        private int lastPage;
        @SerializedName("has_next_page")
        private boolean hasNextPage;
        public int     getLastPage()    { return lastPage; }
        public boolean hasNextPage()    { return hasNextPage; }
    }
}