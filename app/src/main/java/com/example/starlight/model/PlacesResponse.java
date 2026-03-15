package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlacesResponse {

    @SerializedName("results")
    private List<CinemaItem> results;

    @SerializedName("status")
    private String status;

    @SerializedName("error_message")
    private String errorMessage;

    public List<CinemaItem> getResults()   { return results; }
    public String           getStatus()    { return status; }
    public String           getErrorMessage() { return errorMessage; }

    public boolean isOk() {
        return "OK".equals(status) || "ZERO_RESULTS".equals(status);
    }
}