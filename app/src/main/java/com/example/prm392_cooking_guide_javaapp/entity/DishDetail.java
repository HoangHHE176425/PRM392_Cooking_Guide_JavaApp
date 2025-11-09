package com.example.prm392_cooking_guide_javaapp.entity;
public class DishDetail {
    private int id;
    private String name;
    private String description;
    private String imageUrl;
    private String cookingSteps;
    private String difficultyLevel;
    private int totalCalories;

    public DishDetail() {
    }

    public DishDetail(int id, String name, String description, String imageUrl,
                      String cookingSteps, String difficultyLevel, int totalCalories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cookingSteps = cookingSteps;
        this.difficultyLevel = difficultyLevel;
        this.totalCalories = totalCalories;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCookingSteps() {
        return cookingSteps;
    }

    public void setCookingSteps(String cookingSteps) {
        this.cookingSteps = cookingSteps;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }
}
