package com.example.prm392_cooking_guide_javaapp.entity;
import android.util.Log;
import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DishDAO {
    private static final String TAG = "DishDAO";

    /**
     * Lấy thông tin chi tiết món ăn theo ID
     */
    public DishDetail getDishDetailById(int dishId) {
        DishDetail dishDetail = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Log.d(TAG, "📡 Đang truy vấn món ăn với ID: " + dishId);

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                Log.e(TAG, "❌ Không thể kết nối database");
                return null;
            }

            // Query lấy thông tin món ăn và tổng calories
            String query = "SELECT d.id, d.name, d.description, d.image_url, " +
                    "d.cooking_steps, d.difficulty_level, " +
                    "ISNULL(SUM(di.calories_per_unit), 0) as total_calories " +
                    "FROM Dishes d " +
                    "LEFT JOIN Dish_Ingredients di ON d.id = di.dish_id " +
                    "WHERE d.id = ? " +
                    "GROUP BY d.id, d.name, d.description, d.image_url, " +
                    "d.cooking_steps, d.difficulty_level";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, dishId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                dishDetail = new DishDetail();
                dishDetail.setId(rs.getInt("id"));
                dishDetail.setName(rs.getString("name"));
                dishDetail.setDescription(rs.getString("description"));
                dishDetail.setImageUrl(rs.getString("image_url"));
                dishDetail.setCookingSteps(rs.getString("cooking_steps"));
                dishDetail.setDifficultyLevel(rs.getString("difficulty_level"));
                dishDetail.setTotalCalories(rs.getInt("total_calories"));

                Log.d(TAG, "✅ Lấy thông tin món ăn thành công: " + dishDetail.getName());
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Lỗi khi lấy chi tiết món ăn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi đóng connection: " + e.getMessage());
            }
        }

        return dishDetail;
    }

    /**
     * Lấy danh sách nguyên liệu của món ăn
     */
    public List<DishIngredient> getIngredientsByDishId(int dishId) {
        List<DishIngredient> ingredients = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                Log.e(TAG, "❌ Không thể kết nối database");
                return ingredients;
            }

            String query = "SELECT id, dish_id, ingredient_name, quantity, " +
                    "calories_per_unit FROM Dish_Ingredients " +
                    "WHERE dish_id = ?";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, dishId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                DishIngredient ingredient = new DishIngredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setDishId(rs.getInt("dish_id"));
                ingredient.setIngredientName(rs.getString("ingredient_name"));
                ingredient.setQuantity(rs.getString("quantity"));
                ingredient.setCaloriesPerUnit(rs.getInt("calories_per_unit"));
                ingredients.add(ingredient);
            }

            Log.d(TAG, "✅ Lấy " + ingredients.size() + " nguyên liệu thành công");

        } catch (Exception e) {
            Log.e(TAG, "💥 Lỗi khi lấy danh sách nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi đóng connection: " + e.getMessage());
            }
        }

        return ingredients;
    }
}