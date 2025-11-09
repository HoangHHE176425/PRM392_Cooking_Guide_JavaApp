package com.example.prm392_cooking_guide_javaapp.connectDB;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_cooking_guide_javaapp.R;
import com.example.prm392_cooking_guide_javaapp.entity.DishDetailActivity;

import java.sql.Connection;

public class TestConnectionActivity extends AppCompatActivity {
    private TextView statusTextView;
    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_connection);

        statusTextView = findViewById(R.id.statusTextView);
        testButton = findViewById(R.id.testButton);

        testButton.setOnClickListener(v -> testConnection());
    }

    private void testConnection() {
        Intent intent = new Intent(TestConnectionActivity.this, DishDetailActivity.class);

        // (Tuỳ bạn) Gửi id món ăn muốn test qua
        intent.putExtra("DISH_ID", 1);

        // Chuyển sang màn DishDetailActivity
        startActivity(intent);
        statusTextView.setText("Testing connection...");
        testButton.setEnabled(false);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        connection.close();
                        return "SUCCESS: Connected to SQL Server!";
                    } else {
                        return "FAILED: Cannot connect to SQL Server";
                    }
                } catch (Exception e) {
                    return "ERROR: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                statusTextView.setText(result);
                testButton.setEnabled(true);
            }
        }.execute();
    }
}

