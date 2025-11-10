package com.example.prm392_cooking_guide_javaapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome, tvUserInfo;
    private Button btnProfile, btnLogout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        initViews();
        displayUserInfo();
        setupListeners();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void displayUserInfo() {
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullName = sharedPreferences.getString("fullName", "");
        String username = sharedPreferences.getString("username", "");
        String email = sharedPreferences.getString("email", "");
        String role = sharedPreferences.getString("role", "");
        String bio = sharedPreferences.getString("bio", "");

        if (tvWelcome != null) {
            tvWelcome.setText("Xin chào, " + fullName + "! 👋");
        }

        if (tvUserInfo != null) {
            String userInfo = "👤 Tên đăng nhập: " + username + "\n" +
                            "📧 Email: " + email + "\n" +
                            "🎭 Vai trò: " + (role.equals("admin") ? "Quản trị viên" : "Người dùng") + "\n" +
                            "📝 Giới thiệu: " + (bio.isEmpty() ? "Chưa cập nhật" : bio);
            tvUserInfo.setText(userInfo);
        }
    }

    private void setupListeners() {
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> openProfile());
        }
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        // Clear login data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        // Go back to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

