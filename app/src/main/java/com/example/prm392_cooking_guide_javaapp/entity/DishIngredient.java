package com.example.prm392_cooking_guide_javaapp.entity;

public class DishIngredient {
    private int id;
    private int dishId;
    private String ingredientName;
    private String quantity;
    private int caloriesPerUnit;

    public DishIngredient() {
    }

    public DishIngredient(int id, int dishId, String ingredientName,
                          String quantity, int caloriesPerUnit) {
        this.id = id;
        this.dishId = dishId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.caloriesPerUnit = caloriesPerUnit;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public int getCaloriesPerUnit() {
        return caloriesPerUnit;
    }

    public void setCaloriesPerUnit(int caloriesPerUnit) {
        this.caloriesPerUnit = caloriesPerUnit;
    }
}

