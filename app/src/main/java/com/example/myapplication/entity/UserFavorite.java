package com.example.myapplication.entity;

public class UserFavorite {
    private int id;
    private int userId;
    private int dishesId;
    private String createdAt;

    // Constructor rỗng
    public UserFavorite() {}

    // Constructor đầy đủ
    public UserFavorite(int userId, int dishesId) {
        this.userId = userId;
        this.dishesId = dishesId;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getDishesId() { return dishesId; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setDishesId(int dishesId) { this.dishesId = dishesId; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
