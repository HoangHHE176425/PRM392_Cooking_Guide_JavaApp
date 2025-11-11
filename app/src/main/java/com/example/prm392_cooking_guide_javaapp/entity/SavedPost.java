package com.example.prm392_cooking_guide_javaapp.entity;

import java.util.Date;

public class SavedPost {
    private int id;
    private int userId;
    private int postId;
    private Date savedAt;

    public SavedPost() {}

    public SavedPost(int id, int userId, int postId, Date savedAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.savedAt = savedAt;
    }

    // Getters & Setters
    // ...
}
