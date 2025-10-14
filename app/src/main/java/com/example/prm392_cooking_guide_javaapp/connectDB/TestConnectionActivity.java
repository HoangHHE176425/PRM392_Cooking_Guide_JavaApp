package com.example.prm392_cooking_guide_javaapp.connectDB;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

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

