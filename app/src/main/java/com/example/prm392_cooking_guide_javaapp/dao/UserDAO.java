package com.example.prm392_cooking_guide_javaapp.dao;

import com.example.prm392_cooking_guide_javaapp.connectDB.DatabaseConnection;
import com.example.prm392_cooking_guide_javaapp.entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private static final String TAG = "UserDAO";

    /**
     * Đăng nhập user với username và password
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return User object nếu đăng nhập thành công, null nếu thất bại
     */
    public User login(String username, String password) {
        User user = null;
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setString(1, username);
            statement.setString(2, password);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setBio(resultSet.getString("bio"));
                user.setFullName(resultSet.getString("full_name"));
                user.setAvatarUrl(resultSet.getString("avatar_url"));
                user.setRole(resultSet.getString("role"));
                user.setCreatedAt(resultSet.getString("created_at"));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
        
        return user;
    }

    /**
     * Đăng ký user mới
     * @param user User object chứa thông tin đăng ký
     * @return true nếu đăng ký thành công, false nếu thất bại
     */
    public boolean register(User user) {
        String query = "INSERT INTO Users (username, email, password, bio, full_name, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getBio());
            statement.setString(5, user.getFullName());
            statement.setString(6, user.getRole() != null ? user.getRole() : "user");
            
            int result = statement.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi đăng ký: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    public boolean isUsernameExists(String username) {
        String query = "SELECT COUNT(*) FROM Users WHERE username = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     * @param email Email cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    public boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM Users WHERE email = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Lấy thông tin user theo ID
     * @param userId ID của user
     * @return User object hoặc null nếu không tìm thấy
     */
    public User getUserById(int userId) {
        User user = null;
        String query = "SELECT * FROM Users WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setBio(resultSet.getString("bio"));
                user.setFullName(resultSet.getString("full_name"));
                user.setAvatarUrl(resultSet.getString("avatar_url"));
                user.setRole(resultSet.getString("role"));
                user.setCreatedAt(resultSet.getString("created_at"));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi lấy thông tin user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return user;
    }
}
