package com.example.myapplication.entity;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String bio;
    private String fullName;
    private String avatarUrl;
    private String role;
    private String createdAt;

    // Constructor rỗng
    public User() {}

    // Constructor đầy đủ
    public User(String username, String email, String password, String bio,
                String fullName, String avatarUrl, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    // Constructor cho đăng ký cơ bản
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = "user"; // mặc định là user
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getBio() { return bio; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setBio(String bio) { this.bio = bio; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

