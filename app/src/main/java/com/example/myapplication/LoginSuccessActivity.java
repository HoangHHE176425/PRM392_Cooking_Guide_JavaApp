package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginSuccessActivity extends AppCompatActivity {
    private TextView welcomeTextView, userInfoTextView;
    private Button continueButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_success);

        initViews();
        displayUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView);
        userInfoTextView = findViewById(R.id.userInfoTextView);
        continueButton = findViewById(R.id.continueButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void displayUserInfo() {
        // Lấy thông tin user từ Intent
        String username = getIntent().getStringExtra("USERNAME");
        String email = getIntent().getStringExtra("EMAIL");
        String fullName = getIntent().getStringExtra("FULL_NAME");
        String role = getIntent().getStringExtra("ROLE");

        welcomeTextView.setText("Welcome, " + (fullName != null ? fullName : username) + "!");

        String userInfo = "📧 Email: " + email + "\n" +
                "👤 Username: " + username + "\n" +
                "🎭 Role: " + role;
        userInfoTextView.setText(userInfo);
    }

//    private void setupClickListeners() {
//        continueButton.setOnClickListener(v -> {
//            // Chuyển sang MainActivity hoặc màn hình chính
//            Intent intent = new Intent(this, MainActivity.class);
//            // Truyền thông tin user
//            intent.putExtras(getIntent().getExtras());
//            startActivity(intent);
//            finish();
//        });
//
//        logoutButton.setOnClickListener(v -> {
//            // Quay lại màn hình đăng nhập
//            Intent intent = new Intent(this, LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        });
//    }
private void setupClickListeners() {
    continueButton.setOnClickListener(v -> {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    });
}

}
