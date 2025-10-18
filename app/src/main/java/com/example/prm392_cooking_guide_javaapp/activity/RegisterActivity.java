package com.example.prm392_cooking_guide_javaapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.prm392_cooking_guide_javaapp.dao.UserDAO;
import com.example.prm392_cooking_guide_javaapp.entity.User;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtEmail, edtPassword, edtConfirmPassword, edtFullName;
    private Button btnRegister, btnBackToLogin;
    private TextView tvStatus;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initData();
        setupListeners();
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtFullName = findViewById(R.id.edtFullName);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void initData() {
        userDAO = new UserDAO();
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        btnBackToLogin.setOnClickListener(v -> goBackToLogin());
    }

    private void performRegister() {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();

        // Validation
        if (!validateInput(username, email, password, confirmPassword, fullName)) {
            return;
        }

        // Show loading state
        showStatus("Đang tạo tài khoản...", true);
        setLoadingState(true);

        // Perform register in background
        new RegisterTask().execute(username, email, password, fullName);
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword, String fullName) {
        // Check username
        if (TextUtils.isEmpty(username)) {
            showStatus("Vui lòng nhập tên đăng nhập!", false);
            edtUsername.requestFocus();
            return false;
        }
        if (username.length() < 3) {
            showStatus("Tên đăng nhập phải có ít nhất 3 ký tự!", false);
            edtUsername.requestFocus();
            return false;
        }
        if (username.length() > 20) {
            showStatus("Tên đăng nhập không được quá 20 ký tự!", false);
            edtUsername.requestFocus();
            return false;
        }

        // Check email
        if (TextUtils.isEmpty(email)) {
            showStatus("Vui lòng nhập email!", false);
            edtEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatus("Email không hợp lệ!", false);
            edtEmail.requestFocus();
            return false;
        }

        // Check password
        if (TextUtils.isEmpty(password)) {
            showStatus("Vui lòng nhập mật khẩu!", false);
            edtPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            showStatus("Mật khẩu phải có ít nhất 6 ký tự!", false);
            edtPassword.requestFocus();
            return false;
        }

        // Check confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            showStatus("Vui lòng xác nhận mật khẩu!", false);
            edtConfirmPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showStatus("Mật khẩu xác nhận không khớp!", false);
            edtConfirmPassword.requestFocus();
            return false;
        }

        // Check full name
        if (TextUtils.isEmpty(fullName)) {
            showStatus("Vui lòng nhập họ tên!", false);
            edtFullName.requestFocus();
            return false;
        }

        return true;
    }

    private class RegisterTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String email = params[1];
            String password = params[2];
            String fullName = params[3];

            try {
                // Check if username already exists
                if (userDAO.isUsernameExists(username)) {
                    errorMessage = "Tên đăng nhập đã tồn tại!";
                    return false;
                }

                // Check if email already exists
                if (userDAO.isEmailExists(email)) {
                    errorMessage = "Email đã được sử dụng!";
                    return false;
                }

                // Create new user
                User newUser = new User(username, email, password, "", fullName, "user");
                return userDAO.register(newUser);

            } catch (Exception e) {
                errorMessage = "Lỗi hệ thống: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            setLoadingState(false);
            
            if (success) {
                showStatus("✅ Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.", true);
                
                Toast.makeText(RegisterActivity.this, 
                    "Đăng ký thành công!\nBạn có thể đăng nhập với tài khoản vừa tạo.", 
                    Toast.LENGTH_LONG).show();
                
                // Delay before navigation for better UX
                new android.os.Handler().postDelayed(() -> {
                    goBackToLogin();
                }, 2000);
                
            } else {
                showStatus("❌ " + errorMessage, false);
                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goBackToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setText(message);
        tvStatus.setTextColor(isSuccess ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnBackToLogin.setEnabled(!isLoading);
        edtUsername.setEnabled(!isLoading);
        edtEmail.setEnabled(!isLoading);
        edtPassword.setEnabled(!isLoading);
        edtConfirmPassword.setEnabled(!isLoading);
        edtFullName.setEnabled(!isLoading);
        
        if (isLoading) {
            btnRegister.setText("Đang tạo tài khoản...");
        } else {
            btnRegister.setText("Đăng ký");
        }
    }

    @Override
    public void onBackPressed() {
        goBackToLogin();
    }
}
