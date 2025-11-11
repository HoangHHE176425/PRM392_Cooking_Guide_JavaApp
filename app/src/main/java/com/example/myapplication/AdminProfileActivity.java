package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.snackbar.Snackbar;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;

public class AdminProfileActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView usernameTextView, emailTextView, fullNameTextView, bioTextView;
    private Button editProfileButton, backToDashboardButton, logoutButton;
    private int adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        initViews();
        setupAdminInfo();
        setupClickListeners();
        loadAdminFromDatabase();
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        bioTextView = findViewById(R.id.bioTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupAdminInfo() {
        // Lấy admin ID từ intent
        adminId = getIntent().getIntExtra("ADMIN_ID", 0);

        // Nếu không có từ intent, thử lấy từ các source khác
        if (adminId == 0) {
            adminId = getIntent().getIntExtra("USER_ID", 0);
        }

        // Hiển thị thông tin cơ bản từ intent (nếu có)
        String username = getIntent().getStringExtra("USERNAME");
        String email = getIntent().getStringExtra("EMAIL");
        String fullName = getIntent().getStringExtra("FULL_NAME");
        String bio = getIntent().getStringExtra("BIO");

        if (username != null) {
            usernameTextView.setText("@" + username);
        }
        if (email != null) {
            emailTextView.setText(email);
        }
        if (fullName != null) {
            fullNameTextView.setText(fullName);
        } else {
            fullNameTextView.setText("Chưa cập nhật");
        }
        if (bio != null) {
            bioTextView.setText(bio);
        } else {
            bioTextView.setText("Quản trị viên hệ thống ứng dụng nấu ăn. Luôn sẵn sàng hỗ trợ và cải thiện trải nghiệm người dùng.");
        }
    }

    private void loadAdminFromDatabase() {
        if (adminId != 0) {
            UserDAO.getUserById(adminId, new UserDAO.UserCallback() {
                @Override
                public void onSuccess(User admin) {
                    // Cập nhật tất cả thông tin từ database
                    usernameTextView.setText("@" + admin.getUsername());
                    emailTextView.setText(admin.getEmail());
                    fullNameTextView.setText(admin.getFullName() != null ?
                            admin.getFullName() : "Administrator");
                    bioTextView.setText(admin.getBio() != null ?
                            admin.getBio() : "Quản trị viên hệ thống ứng dụng nấu ăn. Luôn sẵn sàng hỗ trợ và cải thiện trải nghiệm người dùng.");
                    loadAdminAvatar(admin.getAvatarUrl());
                }

                @Override
                public void onError(String error) {
                    showSnackbar("❌ Lỗi tải thông tin admin: " + error, false);
                }
            });
        }
    }

    private void loadAdminAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (bitmap != null) {
                        profileImageView.setImageBitmap(bitmap);
                    }
                }
                // Nếu sử dụng URL từ server, có thể dùng Glide hoặc Picasso
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEditProfileActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivityForResult(intent, 100);
        });

        backToDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.putExtra("USER_ID", adminId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload admin info after editing
            showSnackbar("✅ Thông tin admin đã được cập nhật", true);
            loadAdminFromDatabase();
        }
    }

    // Method để hiển thị Snackbar đẹp
    private void showSnackbar(String message, boolean isSuccess) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar;

        if (isSuccess) {
            snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
            snackbar.setAction("ĐÓNG", v -> snackbar.dismiss());
            snackbar.setActionTextColor(getResources().getColor(android.R.color.white));
        }

        snackbar.setTextColor(getResources().getColor(android.R.color.white));
        snackbar.show();
    }
}
