package com.example.prm392_cooking_guide_javaapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.prm392_cooking_guide_javaapp.dao.UserDAO;
import com.example.prm392_cooking_guide_javaapp.entity.User;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtFullName, edtEmail, edtBio, edtRole;
    private ImageView imgAvatar;
    private Button btnBack, btnChooseImage, btnSave;
    private TextView tvStatus;
    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    private String selectedImageBase64;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize image picker launcher (from gallery)
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        loadImageFromUri(imageUri);
                    }
                }
            }
        );

        initViews();
        initData();
        loadUserInfo();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtBio = findViewById(R.id.edtBio);
        edtRole = findViewById(R.id.edtRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSave = findViewById(R.id.btnSave);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void initData() {
        userDAO = new UserDAO();
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
    }

    private void loadUserInfo() {
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load user info in background
        new LoadUserTask().execute(userId);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> goBackToHome());
        btnChooseImage.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void goBackToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void chooseImage() {
        // Open gallery to choose image
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadImageFromUri(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                // Resize image to reduce size (400x400 max for avatar)
                bitmap = resizeBitmap(bitmap, 400);
                selectedImageBase64 = bitmapToBase64(bitmap);
                imgAvatar.setImageBitmap(bitmap);
                Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Đang tải thông tin, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String bio = edtBio.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            showStatus("Vui lòng nhập họ và tên!", false);
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            showStatus("Vui lòng nhập email!", false);
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatus("Email không hợp lệ!", false);
            edtEmail.requestFocus();
            return;
        }

        // Check if email is already used by another user
        if (!email.equals(currentUser.getEmail()) && userDAO.isEmailExists(email, currentUser.getId())) {
            showStatus("Email này đã được sử dụng bởi tài khoản khác!", false);
            edtEmail.requestFocus();
            return;
        }

        // Validation: User must have an avatar
        String avatarUrl = selectedImageBase64 != null ? selectedImageBase64 : currentUser.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            showStatus("Vui lòng chọn ảnh đại diện!", false);
            Toast.makeText(this, "Ảnh đại diện là bắt buộc!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if Base64 string is too long (warn if > 500KB)
        int sizeKB = avatarUrl.length() / 1024;
        if (sizeKB > 500) {
            showStatus("Cảnh báo: Ảnh quá lớn (" + sizeKB + " KB). Vui lòng chọn ảnh nhỏ hơn!", false);
            Toast.makeText(this, "Ảnh quá lớn! Vui lòng chọn ảnh nhỏ hơn.", Toast.LENGTH_LONG).show();
            return;
        }

        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setBio(bio);
        currentUser.setAvatarUrl(avatarUrl);

        // Show loading state
        showStatus("Đang lưu thay đổi...", true);
        setLoadingState(true);

        // Save in background
        new UpdateUserTask().execute(currentUser);
    }

    private class LoadUserTask extends AsyncTask<Integer, Void, User> {
        @Override
        protected User doInBackground(Integer... params) {
            try {
                int userId = params[0];
                return userDAO.getUserById(userId);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                currentUser = user;
                displayUserInfo(user);
            } else {
                Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private class UpdateUserTask extends AsyncTask<User, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(User... params) {
            try {
                User user = params[0];
                return userDAO.updateUser(user);
            } catch (Exception e) {
                errorMessage = "Lỗi hệ thống: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            setLoadingState(false);

            if (success) {
                // Update SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fullName", currentUser.getFullName());
                editor.putString("email", currentUser.getEmail());
                editor.putString("bio", currentUser.getBio());
                editor.putString("avatarUrl", currentUser.getAvatarUrl());
                editor.apply();
                
                // Clear selected image after saving
                selectedImageBase64 = null;

                showStatus("✅ Cập nhật thông tin thành công!", true);
                Toast.makeText(ProfileActivity.this, "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();

                // Delay before hiding status
                new android.os.Handler().postDelayed(() -> {
                    tvStatus.setVisibility(View.GONE);
                }, 2000);
            } else {
                String errorMsg = !errorMessage.isEmpty() ? errorMessage : "Không thể cập nhật thông tin!";
                showStatus("❌ " + errorMsg, false);
                Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void displayUserInfo(User user) {
        if (edtUsername != null) {
            edtUsername.setText(user.getUsername());
        }
        if (edtFullName != null) {
            edtFullName.setText(user.getFullName());
        }
        if (edtEmail != null) {
            edtEmail.setText(user.getEmail());
        }
        if (edtBio != null) {
            edtBio.setText(user.getBio() != null ? user.getBio() : "");
        }
        if (edtRole != null) {
            String roleText = user.getRole() != null && user.getRole().equals("admin") ? "Quản trị viên" : "Người dùng";
            edtRole.setText(roleText);
        }
        
        // Display avatar
        if (imgAvatar != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                Bitmap bitmap = base64ToBitmap(user.getAvatarUrl());
                if (bitmap != null) {
                    imgAvatar.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                // Silent fail - avatar will not be displayed
            }
        }
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Reduce quality to 60% to minimize file size
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    
    private Bitmap base64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setText(message);
        tvStatus.setTextColor(isSuccess ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.holo_red_dark));
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean isLoading) {
        btnSave.setEnabled(!isLoading);
        edtFullName.setEnabled(!isLoading);
        edtEmail.setEnabled(!isLoading);
        edtBio.setEnabled(!isLoading);

        if (isLoading) {
            btnSave.setText("Đang lưu...");
        } else {
            btnSave.setText("💾 Lưu thay đổi");
        }
    }
}

