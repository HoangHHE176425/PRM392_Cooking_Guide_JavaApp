package com.example.myapplication.entity;

public class Dish {
    private int id;
    private String name;
    private String description;
    private int userId;
    private String imageUrl;
    private String cookingSteps;
    private String ingredient;
    private String difficultyLevel;
    private String createdAt;

    // Constructor rỗng
    public Dish() {}

    // Constructor đầy đủ
    public Dish(String name, String description, int userId, String imageUrl,
                String cookingSteps, String ingredient, String difficultyLevel) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.cookingSteps = cookingSteps;
        this.ingredient = ingredient;
        this.difficultyLevel = difficultyLevel;
    }

    // Constructor cơ bản
    public Dish(String name, String description, int userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getUserId() { return userId; }
    public String getImageUrl() { return imageUrl; }
    public String getCookingSteps() { return cookingSteps; }
    public String getIngredient() { return ingredient; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCookingSteps(String cookingSteps) { this.cookingSteps = cookingSteps; }
    public void setIngredient(String ingredient) { this.ingredient = ingredient; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
