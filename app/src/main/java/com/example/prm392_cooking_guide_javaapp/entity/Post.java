package com.example.prm392_cooking_guide_javaapp.entity;

public class Post {
    private int id;
    private int userId;
    private String username;     // 👤 Tên người đăng
    private String avatarUrl;    // 🖼️ Ảnh đại diện người đăng
    private String content;      // 📝 Nội dung bài viết
    private String mediaUrl;     // 📸 hoặc 🎥 link ảnh/video
    private String mediaType;    // "image", "video", "none"
    private String visibility;   // "public", "friends", "private"
    private String createdAt;    // thời gian đăng (chuỗi để hiển thị nhanh)
    private User user;           // Optional: nếu muốn lấy đầy đủ user info

    // ✅ Constructors
    public Post() {
    }

    public Post(int id, int userId, String username, String avatarUrl,
                String content, String mediaUrl, String mediaType,
                String visibility, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.visibility = visibility;
        this.createdAt = createdAt;
    }

    // ✅ Getter & Setter methods
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }
    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
