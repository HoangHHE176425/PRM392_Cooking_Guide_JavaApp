package com.example.prm392_cooking_guide_javaapp.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;
import com.example.prm392_cooking_guide_javaapp.entity.Comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    private static final String TAG = "CommentDAO";

    // ✅ Tiện ích: ép mọi callback chạy trên Main Thread
    private static void runOnMain(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }

    // ✅ Lấy danh sách bình luận theo bài post
    public static void getCommentsByPostId(int postId, CommentCallback callback) {
        new Thread(() -> {
            List<Comment> comments = new ArrayList<>();
            try {
                Connection connection = DatabaseConnection.getConnection();
                if (connection == null) {
                    runOnMain(() -> callback.onError("Không thể kết nối SQL Server"));
                    return;
                }

                String sql = "SELECT c.id, c.content, c.created_at, u.username " +
                        "FROM Comments c " +
                        "JOIN Users u ON c.user_id = u.id " +
                        "WHERE c.post_id = ? ORDER BY c.created_at ASC";

                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, postId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setContent(rs.getString("content"));
                    comment.setCreatedAt(rs.getString("created_at"));
                    comment.setUsername(rs.getString("username"));
                    comments.add(comment);
                }

                rs.close();
                stmt.close();
                connection.close();

                // ✅ Đưa callback về Main Thread để cập nhật UI
                runOnMain(() -> callback.onSuccess(comments));

            } catch (Exception e) {
                Log.e(TAG, "❌ Lỗi khi tải bình luận", e);
                runOnMain(() -> callback.onError("Lỗi khi tải bình luận: " + e.getMessage()));
            }
        }).start();
    }

    // ✅ Thêm bình luận mới
    public static void addComment(Comment comment, AddCommentCallback callback) {
        new Thread(() -> {
            try {
                Connection connection = DatabaseConnection.getConnection();
                if (connection == null) {
                    runOnMain(() -> callback.onError("Không thể kết nối SQL Server"));
                    return;
                }

                String sql = "INSERT INTO Comments (post_id, user_id, content, created_at) " +
                        "VALUES (?, ?, ?, GETDATE())";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, comment.getPostId());
                stmt.setInt(2, comment.getUserId());
                stmt.setString(3, comment.getContent());

                int rows = stmt.executeUpdate();

                stmt.close();
                connection.close();

                if (rows > 0)
                    runOnMain(callback::onSuccess);
                else
                    runOnMain(() -> callback.onError("Không thêm được bình luận"));

            } catch (Exception e) {
                Log.e(TAG, "❌ Lỗi khi thêm bình luận", e);
                runOnMain(() -> callback.onError("Lỗi khi thêm bình luận: " + e.getMessage()));
            }
        }).start();
    }

    // ================== INTERFACES CALLBACK ==================
    public interface CommentCallback {
        void onSuccess(List<Comment> comments);
        void onError(String error);
    }

    public interface AddCommentCallback {
        void onSuccess();
        void onError(String error);
    }
}
