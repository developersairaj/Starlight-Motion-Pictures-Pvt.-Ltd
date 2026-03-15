package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoResponse {

    @SerializedName("results")
    private List<Video> results;

    public List<Video> getResults() { return results; }

    public static class Video {
        @SerializedName("key")  private String key;
        @SerializedName("site") private String site;
        @SerializedName("type") private String type;
        @SerializedName("name") private String name;

        public String getKey()  { return key; }
        public String getSite() { return site; }
        public String getType() { return type; }
        public String getName() { return name; }

        public boolean isYouTubeTrailer() {
            return "YouTube".equalsIgnoreCase(site)
                    && "Trailer".equalsIgnoreCase(type);
        }

        public String getYouTubeUrl() {
            return "https://www.youtube.com/watch?v=" + key;
        }
    }
}