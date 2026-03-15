package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;

public class OmdbResponse {

    @SerializedName("imdbRating") private String imdbRating;
    @SerializedName("imdbVotes")  private String imdbVotes;
    @SerializedName("Awards")     private String awards;
    @SerializedName("Rated")      private String rated;
    @SerializedName("Runtime")    private String runtime;
    @SerializedName("Director")   private String director;
    @SerializedName("Actors")     private String actors;
    @SerializedName("Response")   private String response;

    public String getImdbRating() { return imdbRating != null ? imdbRating : "N/A"; }
    public String getImdbVotes()  { return imdbVotes  != null ? imdbVotes  : ""; }
    public String getAwards()     { return awards     != null ? awards     : ""; }
    public String getRated()      { return rated      != null ? rated      : ""; }
    public String getRuntime()    { return runtime    != null ? runtime    : ""; }
    public String getDirector()   { return director   != null ? director   : ""; }
    public String getActors()     { return actors     != null ? actors     : ""; }
    public boolean isSuccess()    { return "True".equalsIgnoreCase(response); }
}