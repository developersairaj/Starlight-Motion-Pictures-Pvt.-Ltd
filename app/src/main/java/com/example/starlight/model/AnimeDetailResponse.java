package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;

public class AnimeDetailResponse {
    @SerializedName("data")
    private AnimeItem data;
    public AnimeItem getData() { return data; }
}