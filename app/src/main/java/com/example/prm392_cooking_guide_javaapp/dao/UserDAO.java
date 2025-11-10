package com.example.myapplication.dao;

import android.os.AsyncTask;

import com.example.myapplication.connnectDB.DatabaseConnection;
import com.example.myapplication.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String error);
    }
    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }
    public interface RegisterCallback {
        void onSuccess();
        void onError(String error);
    }

    // Đăng nhập
    public static void login(String email, String password, LoginCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Users WHERE email = ? AND password = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, email);
                        statement.setString(2, password); // Nên hash password

                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            User user = new User();
                            user.setId(resultSet.getInt("id"));
                            user.setUsername(resultSet.getString("username"));
                            user.setEmail(resultSet.getString("email"));
                            user.setFullName(resultSet.getString("full_name"));
                            user.setBio(resultSet.getString("bio"));
                            user.setRole(resultSet.getString("role"));

                            connection.close();
                            return user;
                        } else {
                            connection.close();
                            return "Invalid email or password";
                        }
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof User) {
                    callback.onSuccess((User) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Đăng ký
    public static void register(String username, String email, String password, RegisterCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "INSERT INTO Users (username, email, password, bio, role) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, username);
                        statement.setString(2, email);
                        statement.setString(3, password); // Nên hash password
                        statement.setString(4, ""); // bio trống
                        statement.setString(5, "user"); // role mặc định

                        int result = statement.executeUpdate();
                        connection.close();

                        if (result > 0) {
                            return "SUCCESS";
                        } else {
                            return "Registration failed";
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
                if ("SUCCESS".equals(result)) {
                    callback.onSuccess();
                } else {
                    callback.onError(result);
                }
            }
        }.execute();
    }

    public static void getUserById(int userId, UserCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Users WHERE id = ?";
                        PreparedStatement stmt = connection.prepareStatement(query);
                        stmt.setInt(1, userId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            User user = new User();
                            user.setId(rs.getInt("id"));
                            user.setUsername(rs.getString("username"));
                            user.setEmail(rs.getString("email"));
                            user.setPassword(rs.getString("password"));
                            user.setBio(rs.getString("bio"));
                            user.setFullName(rs.getString("full_name"));
                            user.setAvatarUrl(rs.getString("avatar_url"));
                            user.setRole(rs.getString("role"));
                            user.setCreatedAt(rs.getString("created_at"));
                            connection.close();
                            return user;
                        } else {
                            connection.close();
                            return "Không tìm thấy người dùng";
                        }
                    } else {
                        return "Kết nối database thất bại";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof User) {
                    callback.onSuccess((User) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    public static void updateUser(User user, UpdateCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        // Include avatar_url in the UPDATE statement
                        String sql = "UPDATE Users SET username=?, email=?, bio=?, full_name=?, avatar_url=? WHERE id=?";
                        PreparedStatement stmt = connection.prepareStatement(sql);
                        stmt.setString(1, user.getUsername());
                        stmt.setString(2, user.getEmail());
                        stmt.setString(3, user.getBio());
                        stmt.setString(4, user.getFullName());
                        stmt.setString(5, user.getAvatarUrl()); // Add avatar URL
                        stmt.setInt(6, user.getId()); // Note: index changed to 6

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
                else callback.onError("Cập nhật thất bại");
            }
        }.execute();
    }

    // Thêm vào UserDAO.java
    public static void getAllUsers(UsersCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Users ORDER BY created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery();

                        List<User> users = new ArrayList<>();
                        while (resultSet.next()) {
                            User user = new User();
                            user.setId(resultSet.getInt("id"));
                            user.setUsername(resultSet.getString("username"));
                            user.setEmail(resultSet.getString("email"));
                            user.setFullName(resultSet.getString("full_name"));
                            user.setBio(resultSet.getString("bio"));
                            user.setRole(resultSet.getString("role"));
                            user.setCreatedAt(resultSet.getString("created_at"));
                            users.add(user);
                        }
                        connection.close();
                        return users;
                    }
                    return "Connection failed";
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<User>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    public static void deleteUser(int userId, DeleteCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String sql = "DELETE FROM Users WHERE id = ?";
                        PreparedStatement stmt = connection.prepareStatement(sql);
                        stmt.setInt(1, userId);
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
                else callback.onError("Xóa thất bại");
            }
        }.execute();
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }


}
