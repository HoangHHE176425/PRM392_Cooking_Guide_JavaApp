package com.example.prm392_cooking_guide_javaapp.dao;

import android.os.AsyncTask;
import android.util.Log;

import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;
import com.example.prm392_cooking_guide_javaapp.entity.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavedPostDAO {

    public interface SavedPostCallback {
        void onSuccess(List<Post> savedPosts);
        void onError(String error);
    }

    public interface ToggleSaveCallback {
        void onResult(String message);
        void onError(String error);
    }

    // ✅ Lưu hoặc bỏ lưu bài viết
    public static void toggleSaved(int userId, int postId, ToggleSaveCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) return "Connection failed";

                    String checkSql = "SELECT COUNT(*) FROM Saved_Posts WHERE user_id = ? AND post_id = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    checkStmt.setInt(1, userId);
                    checkStmt.setInt(2, postId);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        String deleteSql = "DELETE FROM Saved_Posts WHERE user_id = ? AND post_id = ?";
                        PreparedStatement delStmt = conn.prepareStatement(deleteSql);
                        delStmt.setInt(1, userId);
                        delStmt.setInt(2, postId);
                        delStmt.executeUpdate();
                        return "Đã bỏ lưu bài viết";
                    } else {
                        String insertSql = "INSERT INTO Saved_Posts (user_id, post_id) VALUES (?, ?)";
                        PreparedStatement insStmt = conn.prepareStatement(insertSql);
                        insStmt.setInt(1, userId);
                        insStmt.setInt(2, postId);
                        insStmt.executeUpdate();
                        return "Đã lưu bài viết";
                    }
                } catch (Exception e) {
                    Log.e("SavedPostDAO", "toggleSaved: " + e.getMessage());
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.contains("lưu")) callback.onResult(result);
                else callback.onError(result);
            }
        }.execute();
    }

    // ✅ Lấy danh sách bài viết đã lưu
    public static void getSavedPosts(int userId, SavedPostCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) return "Connection failed";

                    String sql = "SELECT p.* FROM Posts p " +
                            "INNER JOIN Saved_Posts s ON p.id = s.post_id " +
                            "WHERE s.user_id = ? ORDER BY s.saved_at DESC";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, userId);
                    ResultSet rs = ps.executeQuery();

                    List<Post> posts = new ArrayList<>();
                    while (rs.next()) {
                        Post post = new Post();
                        post.setId(rs.getInt("id"));
                        post.setUserId(rs.getInt("user_id"));
                        post.setContent(rs.getString("content"));
                        post.setMediaUrl(rs.getString("media_url"));
                        post.setMediaType(rs.getString("media_type"));
                        post.setVisibility(rs.getString("visibility"));
                        post.setCreatedAt(rs.getString("created_at"));
                        posts.add(post);
                    }
                    return posts;
                } catch (Exception e) {
                    Log.e("SavedPostDAO", "getSavedPosts: " + e.getMessage());
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) callback.onSuccess((List<Post>) result);
                else callback.onError(result.toString());
            }
        }.execute();
    }
}
