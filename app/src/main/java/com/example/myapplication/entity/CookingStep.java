package com.example.myapplication.entity;

public class CookingStep {
    private int stepNumber;
    private String title;
    private String description;

    public CookingStep(int stepNumber, String title, String description) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
    }

    public int getStepNumber() { return stepNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
