package com.example.prm392_cooking_guide_javaapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.prm392_cooking_guide_javaapp.dao.UserDAO;
import com.example.prm392_cooking_guide_javaapp.activity.HomeActivity;
import com.example.prm392_cooking_guide_javaapp.entity.User;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword;
    private Button btnLogin, btnRegister;
    private TextView tvStatus;
    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initData();
        setupListeners();
        checkLoginStatus();
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void initData() {
        userDAO = new UserDAO();
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnRegister.setOnClickListener(v -> openRegisterActivity());
    }

    private void checkLoginStatus() {
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            goToHomeActivity();
        }
    }

    private void performLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            showStatus("Vui lòng nhập tên đăng nhập!", false);
            edtUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showStatus("Vui lòng nhập mật khẩu!", false);
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 3) {
            showStatus("Mật khẩu phải có ít nhất 3 ký tự!", false);
            edtPassword.requestFocus();
            return;
        }

        // Show loading state
        showStatus("Đang đăng nhập...", true);
        setLoadingState(true);

        // Perform login in background
        new LoginTask().execute(username, password);
    }

    private class LoginTask extends AsyncTask<String, Void, User> {
        private String errorMessage = "";

        @Override
        protected User doInBackground(String... params) {
            try {
                String username = params[0];
                String password = params[1];
                return userDAO.login(username, password);
            } catch (Exception e) {
                errorMessage = "Lỗi hệ thống: " + e.getMessage();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            setLoadingState(false);
            
            if (user != null) {
                // Login successful
                saveLoginInfo(user);
                showStatus("✅ Đăng nhập thành công! Chào mừng " + user.getFullName(), true);
                
                Toast.makeText(LoginActivity.this, 
                    "Đăng nhập thành công!\nXin chào " + user.getFullName() + " (" + user.getRole() + ")", 
                    Toast.LENGTH_LONG).show();
                
                // Delay before navigation for better UX
                new android.os.Handler().postDelayed(() -> {
                    goToHomeActivity();
                }, 2000);
                
            } else {
                // Login failed
                String errorMsg = !errorMessage.isEmpty() ? errorMessage : "Tên đăng nhập hoặc mật khẩu không đúng!";
                showStatus("❌ " + errorMsg, false);
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                edtPassword.setText("");
                edtPassword.requestFocus();
            }
        }
    }

    private void saveLoginInfo(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("fullName", user.getFullName());
        editor.putString("bio", user.getBio());
        editor.putString("avatarUrl", user.getAvatarUrl());
        editor.putString("role", user.getRole());
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setText(message);
        tvStatus.setTextColor(isSuccess ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        edtUsername.setEnabled(!isLoading);
        edtPassword.setEnabled(!isLoading);
        
        if (isLoading) {
            btnLogin.setText("Đang đăng nhập...");
        } else {
            btnLogin.setText("Đăng nhập");
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from closing the app
        moveTaskToBack(true);
    }
}
