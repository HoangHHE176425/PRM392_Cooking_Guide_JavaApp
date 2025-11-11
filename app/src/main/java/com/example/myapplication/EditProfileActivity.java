package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;

import java.io.FileOutputStream;

public class EditProfileActivity extends AppCompatActivity {
    private EditText editFullName, editEmail, editBio, editUsername;
    private Button saveProfileButton;
    private ImageView editProfileImageView;
    private int userId;
    private User currentUser;
    private static final int REQUEST_PICK_IMAGE = 1001;
    private String avatarPath; // Đường dẫn ảnh local

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editUsername = findViewById(R.id.editUsername);
        editProfileImageView = findViewById(R.id.editProfileImageView);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editBio = findViewById(R.id.editBio);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        userId = getIntent().getIntExtra("USER_ID", 0);
        ImageView editAvatarButton = findViewById(R.id.editAvatarButton);
        editAvatarButton.setOnClickListener(v -> openImagePicker());
        UserDAO.getUserById(userId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                editUsername.setText(user.getUsername());
                editFullName.setText(user.getFullName());
                editEmail.setText(user.getEmail());
                editBio.setText(user.getBio());
                loadUserAvatar(user.getAvatarUrl()); // Add this line
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi: " + error, false);
            }
        });

        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), REQUEST_PICK_IMAGE);
    }

    private void saveProfile() {
        String username = editUsername.getText().toString().trim();
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            showSnackbar("⚠️ Vui lòng nhập đầy đủ thông tin", false);
            return;
        }

        // Create updated user object
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setUsername(username);
        updatedUser.setPassword(currentUser.getPassword());
        updatedUser.setFullName(fullName);
        updatedUser.setEmail(email);
        updatedUser.setBio(bio);
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setCreatedAt(currentUser.getCreatedAt());

        // Handle avatar update
        if (avatarPath != null && !avatarPath.isEmpty()) {
            updatedUser.setAvatarUrl(avatarPath);
        } else {
            updatedUser.setAvatarUrl(currentUser.getAvatarUrl());
        }

        // Show loading state
        saveProfileButton.setEnabled(false);
        saveProfileButton.setText("Đang lưu...");

        UserDAO.updateUser(updatedUser, new UserDAO.UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showSnackbar("✅ Cập nhật thành công", true);

                    // Delay 2 giây trước khi đóng activity để user thấy được thông báo
                    new android.os.Handler().postDelayed(() -> {
                        setResult(RESULT_OK);
                        finish();
                    }, 2000); // 2 giây
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showSnackbar("❌ Lỗi cập nhật: " + error, false);
                    saveProfileButton.setEnabled(true);
                    saveProfileButton.setText("Lưu thay đổi");
                });
            }
        });

    }

    private void loadUserAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                // If it's a local file path
                if (avatarUrl.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (bitmap != null) {
                        editProfileImageView.setImageBitmap(bitmap);
                    }
                }
                // If you're using network URLs, use Glide or Picasso here
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                editProfileImageView.setImageBitmap(bitmap);

                // Lưu ảnh vào internal storage và lấy đường dẫn
                avatarPath = saveProfileImage(bitmap, "avatar_user_" + userId + ".png");
            } catch (Exception e) {
                showSnackbar("❌ Lỗi chọn ảnh", false);
            }
        }
    }

    // Lưu ảnh vào internal storage
    private String saveProfileImage(Bitmap bitmap, String filename) {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return getFilesDir() + "/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method để hiển thị Snackbar đẹp
    // Method để hiển thị Snackbar đẹp với thời gian hiển thị lâu hơn
    private void showSnackbar(String message, boolean isSuccess) {
        View rootView = findViewById(android.R.id.content);

        if (isSuccess) {
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
            snackbar.setTextColor(getResources().getColor(android.R.color.white));
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
            snackbar.setAction("ĐÓNG", v -> snackbar.dismiss());
            snackbar.setActionTextColor(getResources().getColor(android.R.color.white));
            snackbar.setTextColor(getResources().getColor(android.R.color.white));
            snackbar.show();
        }
    }

}
