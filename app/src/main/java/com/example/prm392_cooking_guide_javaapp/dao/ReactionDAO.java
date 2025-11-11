package com.example.prm392_cooking_guide_javaapp.dao;

import android.os.AsyncTask;
import android.util.Log;

import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReactionDAO {

    public interface ReactionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // ✅ Thêm hoặc cập nhật reaction
    public static void toggleReaction(int userId, int postId, String type, ReactionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) return "Connection failed";

                    String checkSql = "SELECT reaction_type FROM Reactions WHERE user_id = ? AND post_id = ?";
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setInt(1, userId);
                    ps.setInt(2, postId);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        String currentType = rs.getString("reaction_type");
                        if (currentType.equals(type)) {
                            String deleteSql = "DELETE FROM Reactions WHERE user_id = ? AND post_id = ?";
                            PreparedStatement del = conn.prepareStatement(deleteSql);
                            del.setInt(1, userId);
                            del.setInt(2, postId);
                            del.executeUpdate();
                            return "Đã bỏ cảm xúc";
                        } else {
                            String updateSql = "UPDATE Reactions SET reaction_type = ? WHERE user_id = ? AND post_id = ?";
                            PreparedStatement up = conn.prepareStatement(updateSql);
                            up.setString(1, type);
                            up.setInt(2, userId);
                            up.setInt(3, postId);
                            up.executeUpdate();
                            return "Đã đổi cảm xúc sang: " + type;
                        }
                    } else {
                        String insertSql = "INSERT INTO Reactions (post_id, user_id, reaction_type) VALUES (?, ?, ?)";
                        PreparedStatement ins = conn.prepareStatement(insertSql);
                        ins.setInt(1, postId);
                        ins.setInt(2, userId);
                        ins.setString(3, type);
                        ins.executeUpdate();
                        return "Đã thêm cảm xúc: " + type;
                    }
                } catch (Exception e) {
                    Log.e("ReactionDAO", "toggleReaction: " + e.getMessage());
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.contains("Đã")) callback.onSuccess(result);
                else callback.onError(result);
            }
        }.execute();
    }
}
