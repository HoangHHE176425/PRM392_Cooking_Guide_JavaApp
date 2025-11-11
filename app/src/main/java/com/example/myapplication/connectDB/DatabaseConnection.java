package com.example.myapplication.connectDB;

import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection";

    private static final String SERVER_IP = "10.0.2.2"; // USB port forwarding mode
    

    private static final String PORT = "1433";
    private static final String DATABASE_NAME = "Cooking_guide";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123";

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
            // Added socketTimeout and additional parameters for better connection handling
            String connectionURL = "jdbc:jtds:sqlserver://" + SERVER_IP + ":" + PORT +
                    ";databaseName=" + DATABASE_NAME +
                    ";user=" + USERNAME +
                    ";password=" + PASSWORD +
                    ";loginTimeout=30" +
                    ";socketTimeout=30" +
                    ";TDS=8.0" +
                    ";useNTLMv2=false;";

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
