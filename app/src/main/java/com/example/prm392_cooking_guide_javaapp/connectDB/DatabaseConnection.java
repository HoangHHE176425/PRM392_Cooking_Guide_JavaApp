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
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            
            String connectionURL = "jdbc:jtds:sqlserver://" + SERVER_NAME + ":" + PORT +
                    ";databaseName=" + DATABASE_NAME +
                    ";user=" + USERNAME +
                    ";password=" + PASSWORD +
                    ";loginTimeout=30" +
                    ";instance=SQLEXPRESS";

            DriverManager.setLoginTimeout(30);
            return DriverManager.getConnection(connectionURL);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kết nối: " + e.getMessage());
            return null;
        }
    }
}
