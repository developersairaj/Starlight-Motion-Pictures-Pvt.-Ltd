package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;

public class CommunityMessage {

    @SerializedName("id")
    private String id;

    @SerializedName("room")
    private String room;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("message")
    private String message;

    @SerializedName("created_at")
    private String createdAt;

    // Constructor for sending
    public CommunityMessage(String room, String userName,
                            String userEmail, String message) {
        this.room      = room;
        this.userName  = userName;
        this.userEmail = userEmail;
        this.message   = message;
    }

    public String getId()        { return id; }
    public String getRoom()      { return room; }
    public String getUserName()  { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getMessage()   { return message; }
    public String getCreatedAt() { return createdAt; }

    public String getAvatarLetter() {
        if (userName != null && !userName.isEmpty())
            return String.valueOf(userName.charAt(0)).toUpperCase();
        return "?";
    }

    public String getFormattedTime() {
        if (createdAt == null || createdAt.length() < 16) return "";
        // "2026-03-08T14:30:00" → "14:30"
        try {
            String t = createdAt.contains("T")
                    ? createdAt.split("T")[1] : createdAt;
            return t.substring(0, 5);
        } catch (Exception e) {
            return "";
        }
    }
}