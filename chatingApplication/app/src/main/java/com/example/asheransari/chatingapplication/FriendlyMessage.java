package com.example.asheransari.chatingapplication;

/**
 * Created by asher.ansari on 12/7/2016.
 */

public class FriendlyMessage {
    private String text;
    private String name;
    private String photoUrl;

    public FriendlyMessage()
    {

    }

    public FriendlyMessage(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
