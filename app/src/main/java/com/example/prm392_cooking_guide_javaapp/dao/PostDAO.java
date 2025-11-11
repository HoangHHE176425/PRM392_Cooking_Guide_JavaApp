package com.example.prm392_cooking_guide_javaapp.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;
import com.example.prm392_cooking_guide_javaapp.entity.Post;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    private static final String TAG = "PostDAO";

    // ================== INTERFACE CALLBACK ==================
    public interface PostCallback {
        void onSuccess(List<Post> posts);
        void onError(String error);
    }

    public interface AddPostCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface PostDetailCallback {
        void onSuccess(Post post);
        void onError(String error);
    }

    // ================== HÀM HỖ TRỢ ==================
    private static void runOnMain(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }

    // ================== LẤY TẤT CẢ BÀI VIẾT (PUBLIC) ==================
    public static void getAllPosts(PostCallback callback) {
        new Thread(() -> {
            List<Post> posts = new ArrayList<>();
            try {
                Connection connection = DatabaseConnection.getConnection();
                if (connection == null) {
                    runOnMain(() -> callback.onError("Không thể kết nối SQL Server"));
                    return;
                }

                String sql = "SELECT p.*, u.username, u.avatar_url " +
                        "FROM Posts p JOIN Users u ON p.user_id = u.id " +
                        "WHERE p.visibility = 'public' ORDER BY p.created_at DESC";

                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setUserId(rs.getInt("user_id"));
                    post.setContent(rs.getString("content"));
                    post.setMediaType(rs.getString("media_type"));
                    post.setVisibility(rs.getString("visibility"));
                    post.setCreatedAt(rs.getString("created_at"));
                    post.setUsername(rs.getString("username"));
                    post.setAvatarUrl(rs.getString("avatar_url"));

                    // ✅ Chuẩn hóa đường dẫn ảnh
                    String rawUrl = rs.getString("media_url");
                    if (rawUrl != null && rawUrl.contains("content://")) {
                        int idx = rawUrl.indexOf("content://");
                        rawUrl = rawUrl.substring(idx);
                    }
                    post.setMediaUrl(rawUrl);

                    posts.add(post);
                }

                rs.close();
                stmt.close();
                connection.close();

                runOnMain(() -> callback.onSuccess(posts));

            } catch (Exception e) {
                Log.e(TAG, "❌ Lỗi khi tải bài viết", e);
                runOnMain(() -> callback.onError("Lỗi khi tải bài đăng: " + e.getMessage()));
            }
        }).start();
    }

    // ================== LẤY BÀI VIẾT THEO USER ==================
    public static void getPostsByUser(int userId, PostCallback callback) {
        new Thread(() -> {
            List<Post> posts = new ArrayList<>();
            try {
                Connection connection = DatabaseConnection.getConnection();
                if (connection == null) {
                    runOnMain(() -> callback.onError("Không thể kết nối SQL Server"));
                    return;
                }

                String sql = "SELECT p.*, u.username, u.avatar_url " +
                        "FROM Posts p JOIN Users u ON p.user_id = u.id " +
                        "WHERE p.user_id = ? ORDER BY p.created_at DESC";

                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setUserId(rs.getInt("user_id"));
                    post.setContent(rs.getString("content"));
                    post.setMediaType(rs.getString("media_type"));
                    post.setVisibility(rs.getString("visibility"));
                    post.setCreatedAt(rs.getString("created_at"));
                    post.setUsername(rs.getString("username"));
                    post.setAvatarUrl(rs.getString("avatar_url"));

                    String rawUrl = rs.getString("media_url");
                    if (rawUrl != null && rawUrl.contains("content://")) {
                        int idx = rawUrl.indexOf("content://");
                        rawUrl = rawUrl.substring(idx);
                    }
                    post.setMediaUrl(rawUrl);

                    posts.add(post);
                }

                rs.close();
                ps.close();
                connection.close();

                runOnMain(() -> callback.onSuccess(posts));

            } catch (Exception e) {
                Log.e(TAG, "❌ Lỗi khi tải bài viết theo user", e);
                runOnMain(() -> callback.onError("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    // ================== THÊM BÀI VIẾT MỚI ==================
    public static void addPost(Post post, AddPostCallback callback) {
        new Thread(() -> {
            try {
                Connection connection = DatabaseConnection.getConnection();
                if (connection == null) {
                    runOnMain(() -> callback.onError("Không thể kết nối SQL Server"));
                    return;
                }

                String sql = "INSERT INTO Posts (user_id, content, media_url, media_type, visibility) " +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, post.getUserId());
                stmt.setString(2, post.getContent());
                stmt.setString(3, post.getMediaUrl());
                stmt.setString(4, post.getMediaType());
                stmt.setString(5, post.getVisibility());

                int affected = stmt.executeUpdate();

                stmt.close();
                connection.close();

                if (affected > 0)
                    runOnMain(callback::onSuccess);
                else
                    runOnMain(() -> callback.onError("Không thể thêm bài đăng"));

            } catch (Exception e) {
                Log.e(TAG, "❌ Lỗi khi thêm bài đăng", e);
                runOnMain(() -> callback.onError("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    // ================== LẤY CHI TIẾT BÀI VIẾT ==================
    public static void getPostById(int postId, PostDetailCallback callback) {
        new Thread(() -> {
            try {
                Connection connection = DatabaseConnection.getConnection();
                if (connection == null) {
                    runOnMain(() -> callback.onError("Không thể kết nối SQL Server"));
                    return;
                }

                String sql = "SELECT p.*, u.username, u.avatar_url " +
                        "FROM Posts p JOIN Users u ON p.user_id = u.id " +
                        "WHERE p.id = ?";

                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, postId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setUserId(rs.getInt("user_id"));
                    post.setContent(rs.getString("content"));
                    post.setMediaType(rs.getString("media_type"));
                    post.setVisibility(rs.getString("visibility"));
                    post.setCreatedAt(rs.getString("created_at"));
                    post.setUsername(rs.getString("username"));
                    post.setAvatarUrl(rs.getString("avatar_url"));

                    String rawUrl = rs.getString("media_url");
                    if (rawUrl != null && rawUrl.contains("content://")) {
                        int idx = rawUrl.indexOf("content://");
                        rawUrl = rawUrl.substring(idx);
                    }
                    post.setMediaUrl(rawUrl);

                    rs.close();
                    stmt.close();
                    connection.close();

                    runOnMain(() -> callback.onSuccess(post));
                } else {
                    runOnMain(() -> callback.onError("Không tìm thấy bài viết"));
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Lỗi khi lấy chi tiết bài đăng", e);
                runOnMain(() -> callback.onError("Lỗi: " + e.getMessage()));
            }
        }).start();
    }
}
