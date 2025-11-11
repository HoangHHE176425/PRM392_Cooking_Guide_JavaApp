package com.example.prm392_cooking_guide_javaapp.dao;

import android.os.AsyncTask;
import android.util.Log;

import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;
import com.example.prm392_cooking_guide_javaapp.entity.Dish;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DishDAO {

    // Interface cho callback khi lấy danh sách món ăn
    public interface DishCallback {
        void onSuccess(List<Dish> dishes);
        void onError(String error);
    }

    // THÊM INTERFACE NÀY - cho chi tiết món ăn
    public interface DishDetailCallback {
        void onSuccess(Dish dish);
        void onError(String error);
    }

    // Interface cho callback khi thêm/xóa yêu thích
    public interface FavoriteCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // THÊM INTERFACE NÀY - cho kiểm tra yêu thích
    public interface FavoriteCheckCallback {
        void onResult(boolean isFavorite);
    }

    // ... tất cả các method hiện có của bạn ...

    // Method getDishById đã có trong file của bạn - giữ nguyên
    public static void getDishById(int dishId, DishDetailCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Dishes WHERE id = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setInt(1, dishId);
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));

                            Log.d("DishDAO", "Retrieved cooking_steps: " + dish.getCookingSteps());

                            connection.close();
                            return dish;
                        } else {
                            connection.close();
                            return "Dish not found";
                        }
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    Log.e("DishDAO", "Error: " + e.getMessage());
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof Dish) {
                    callback.onSuccess((Dish) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // THÊM METHOD NÀY - để kiểm tra trạng thái yêu thích
    public static void checkIfFavorite(int userId, int dishId, FavoriteCheckCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT COUNT(*) FROM User_Favorites WHERE user_id = ? AND dishes_id = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setInt(1, userId);
                        statement.setInt(2, dishId);
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            boolean isFavorite = resultSet.getInt(1) > 0;
                            connection.close();
                            return isFavorite;
                        }
                        connection.close();
                    }
                    return false;
                } catch (Exception e) {
                    Log.e("DishDAO", "Error checking favorite: " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                callback.onResult(isFavorite);
            }
        }.execute();
    }

    // Thêm method này vào DishDAO.java
    public static void getAllDishes(DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Dishes ORDER BY created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        while (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);
                        }

                        connection.close();
                        return dishes;
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<Dish>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }
    // Thêm method này vào DishDAO.java
    public static void toggleFavorite(int userId, int dishId, FavoriteCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        // Kiểm tra xem đã yêu thích chưa
                        String checkQuery = "SELECT COUNT(*) FROM User_Favorites WHERE user_id = ? AND dishes_id = ?";
                        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                        checkStatement.setInt(1, userId);
                        checkStatement.setInt(2, dishId);
                        ResultSet resultSet = checkStatement.executeQuery();

                        resultSet.next();
                        int count = resultSet.getInt(1);

                        if (count > 0) {
                            // Xóa khỏi yêu thích
                            String deleteQuery = "DELETE FROM User_Favorites WHERE user_id = ? AND dishes_id = ?";
                            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                            deleteStatement.setInt(1, userId);
                            deleteStatement.setInt(2, dishId);
                            deleteStatement.executeUpdate();
                            connection.close();
                            return "Đã xóa khỏi yêu thích";
                        } else {
                            // Thêm vào yêu thích
                            String insertQuery = "INSERT INTO User_Favorites (user_id, dishes_id) VALUES (?, ?)";
                            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                            insertStatement.setInt(1, userId);
                            insertStatement.setInt(2, dishId);
                            insertStatement.executeUpdate();
                            connection.close();
                            return "Đã thêm vào yêu thích";
                        }
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.contains("thêm") || result.contains("xóa")) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(result);
                }
            }
        }.execute();
    }

    // Thêm method này vào DishDAO.java
    public static void getFavoriteDishes(int userId, DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT d.* FROM Dishes d " +
                                "INNER JOIN User_Favorites uf ON d.id = uf.dishes_id " +
                                "WHERE uf.user_id = ? ORDER BY uf.created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setInt(1, userId);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        while (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);
                        }

                        connection.close();
                        return dishes;
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<Dish>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }
    // Thêm method getMostFavoriteDishes
    public static void getMostFavoriteDishes(int limit, DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Log.d("DishDAO", "Starting getMostFavoriteDishes with limit: " + limit);

                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        Log.d("DishDAO", "Database connection successful");

                        String query = "SELECT TOP " + limit + " d.*, COUNT(uf.dishes_id) as favorite_count " +
                                "FROM Dishes d " +
                                "INNER JOIN User_Favorites uf ON d.id = uf.dishes_id " +
                                "GROUP BY d.id, d.name, d.description, d.user_id, d.image_url, " +
                                "d.cooking_steps, d.ingredient, d.difficulty_level, d.created_at " +
                                "ORDER BY favorite_count DESC";

                        Log.d("DishDAO", "Executing query: " + query);

                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        int count = 0;
                        while (resultSet.next()) {
                            count++;
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);

                            Log.d("DishDAO", "Found dish: " + dish.getName() + " with " +
                                    resultSet.getInt("favorite_count") + " favorites");
                        }

                        Log.d("DishDAO", "Total dishes found: " + count);
                        connection.close();
                        return dishes;
                    } else {
                        Log.e("DishDAO", "Database connection failed");
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    Log.e("DishDAO", "Error in getMostFavoriteDishes: " + e.getMessage());
                    e.printStackTrace();
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    List<Dish> dishes = (List<Dish>) result;
                    Log.d("DishDAO", "Returning " + dishes.size() + " dishes to callback");
                    callback.onSuccess(dishes);
                } else {
                    Log.e("DishDAO", "Error result: " + result.toString());
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Thêm method getRecentDishes
    public static void getRecentDishes(DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT TOP 3 * FROM Dishes ORDER BY created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        while (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);
                        }

                        connection.close();
                        return dishes;
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<Dish>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Thêm vào DishDAO.java
    public static void addDish(Dish dish, AddDishCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String sql = "INSERT INTO Dishes (name, description, user_id, image_url, cooking_steps, ingredient, difficulty_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement stmt = connection.prepareStatement(sql);
                        stmt.setString(1, dish.getName());
                        stmt.setString(2, dish.getDescription());
                        stmt.setInt(3, dish.getUserId());
                        stmt.setString(4, dish.getImageUrl());
                        stmt.setString(5, dish.getCookingSteps());
                        stmt.setString(6, dish.getIngredient());
                        stmt.setString(7, dish.getDifficultyLevel());

                        int affected = stmt.executeUpdate();
                        connection.close();
                        return affected > 0;
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) callback.onSuccess();
                else callback.onError("Thêm món ăn thất bại");
            }
        }.execute();
    }
    // Thêm method này vào class DishDAO
    public static void updateDish(Dish dish, AddDishCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String sql = "UPDATE Dishes SET name=?, description=?, image_url=?, cooking_steps=?, ingredient=?, difficulty_level=? WHERE id=?";
                        PreparedStatement stmt = connection.prepareStatement(sql);
                        stmt.setString(1, dish.getName());
                        stmt.setString(2, dish.getDescription());
                        stmt.setString(3, dish.getImageUrl());
                        stmt.setString(4, dish.getCookingSteps());
                        stmt.setString(5, dish.getIngredient());
                        stmt.setString(6, dish.getDifficultyLevel());
                        stmt.setInt(7, dish.getId());

                        int affected = stmt.executeUpdate();
                        connection.close();
                        return affected > 0;
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) callback.onSuccess();
                else callback.onError("Cập nhật món ăn thất bại");
            }
        }.execute();
    }

    public interface DishesCallback {
        void onSuccess(List<Dish> dishes);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface AddDishCallback {
        void onSuccess();
        void onError(String error);
    }

    // Thêm method này vào class DishDAO
    public static void deleteDish(int dishId, DeleteCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String sql = "DELETE FROM Dishes WHERE id = ?";
                        PreparedStatement stmt = connection.prepareStatement(sql);
                        stmt.setInt(1, dishId);
                        int affected = stmt.executeUpdate();
                        connection.close();
                        return affected > 0;
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onError("Xóa món ăn thất bại");
                }
            }
        }.execute();
    }

}
