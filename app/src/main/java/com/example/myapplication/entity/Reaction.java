package com.example.myapplication.entity;

import java.util.Date;

public class Reaction {
    private int id;
    private int postId;
    private int userId;
    private String reactionType; // like, love, haha, wow, sad, angry
    private Date createdAt;

    public Reaction() {}

    public Reaction(int id, int postId, int userId, String reactionType, Date createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.reactionType = reactionType;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    // ...
}
