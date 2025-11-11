package com.example.myapplication.entity;

public class RecentDish {
    private String name;
    private String timeAgo;
    private int imageResource;
    private int dishId;
    private String imageUrl; // Thêm field này

    // Constructor mới với imageUrl
    public RecentDish(String name, String timeAgo, int imageResource, int dishId, String imageUrl) {
        this.name = name;
        this.timeAgo = timeAgo;
        this.imageResource = imageResource;
        this.dishId = dishId;
        this.imageUrl = imageUrl;
    }

    // Constructor cũ (để tương thích)
    public RecentDish(String name, String timeAgo, int imageResource, int dishId) {
        this.name = name;
        this.timeAgo = timeAgo;
        this.imageResource = imageResource;
        this.dishId = dishId;
        this.imageUrl = null;
    }

    // Getter và setter cho imageUrl
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Các getter/setter khác giữ nguyên...


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
}
