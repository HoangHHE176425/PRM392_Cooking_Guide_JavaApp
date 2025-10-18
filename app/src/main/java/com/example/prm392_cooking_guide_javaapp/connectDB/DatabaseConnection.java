package com.example.prm392_cooking_guide_javaapp.connectDB;

import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection";

    // Thay "localhost" bằng IP thực của máy SQL Server
private static final String SERVER_NAME = "192.168.57.101";//    192.168.13.102
//    10.33.8.133

    private static final String PORT = "1433";
    private static final String DATABASE_NAME = "Cooking_guide";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Log.d(TAG, "🔄 Đang kết nối SQL Server Express...");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Load jTDS driver
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Log.d(TAG, "✅ jTDS Driver loaded thành công");

            // Connection string với instance name
            String connectionURL = "jdbc:jtds:sqlserver://" + SERVER_NAME + ":" + PORT +
                    ";databaseName=" + DATABASE_NAME +
                    ";user=" + USERNAME +
                    ";password=" + PASSWORD +
                    ";loginTimeout=30" +
                    ";instance=SQLEXPRESS";

            Log.d(TAG, "🔗 Connecting to: " + SERVER_NAME + ":" + PORT);
            Log.d(TAG, "🗄️ Database: " + DATABASE_NAME);
            Log.d(TAG, "🔗 Connection string: " + connectionURL);

            // Set timeout
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
