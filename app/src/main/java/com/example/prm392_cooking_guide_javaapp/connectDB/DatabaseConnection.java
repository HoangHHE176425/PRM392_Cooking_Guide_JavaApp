package com.example.prm392_cooking_guide_javaapp.connectDB;

import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection";

    // Thay "localhost" bằng IP thực của máy SQL Server
    private static final String SERVER_IP = "10.33.38.45"; // IP máy chạy SQL Server
//    192.168.13.102
//    10.33.8.133

    private static final String PORT = "1433";
    private static final String DATABASE_NAME = "Cooking_guide";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "a123";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Log.d(TAG, "🔄 Đang kết nối SQL Server với jTDS...");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Load jTDS driver (tương thích tốt với Android)
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Log.d(TAG, "✅ jTDS Driver loaded thành công");

            // Connection string cho jTDS
            String connectionURL = "jdbc:jtds:sqlserver://" + SERVER_IP + ":" + PORT +
                    ";databaseName=" + DATABASE_NAME +
                    ";user=" + USERNAME +
                    ";password=" + PASSWORD +
                    ";loginTimeout=30;";

            Log.d(TAG, "🔗 Connecting to: " + SERVER_IP + ":" + PORT);
            Log.d(TAG, "🗄️ Database: " + DATABASE_NAME);

            // Set timeout để tránh treo
            DriverManager.setLoginTimeout(30);
            connection = DriverManager.getConnection(connectionURL);

            if (connection != null && !connection.isClosed()) {
                Log.d(TAG, "🎉 KẾT NỐI THÀNH CÔNG!");
            } else {
                Log.e(TAG, "❌ Connection is null hoặc closed");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 LỖI KẾT NỐI: " + e.getMessage());
            Log.e(TAG, "💥 Chi tiết lỗi: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        return connection;
    }
}
