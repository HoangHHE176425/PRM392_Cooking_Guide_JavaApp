package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import com.example.myapplication.dao.UserDAO;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private CheckBox termsCheckBox;
    private Button continueButton;
    private TextView signInLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        continueButton = findViewById(R.id.continueButton);
        signInLinkTextView = findViewById(R.id.signInLinkTextView);
    }

    private void setupClickListeners() {
        continueButton.setOnClickListener(v -> performSignUp());
        signInLinkTextView.setOnClickListener(v -> goToSignIn());
    }

    private boolean validateInput(String email, String password) {
        boolean valid = true;
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ");
            valid = false;
        } else {
            emailEditText.setError(null);
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Mật khẩu tối thiểu 6 ký tự");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }
        return valid;
    }

    private void performSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!termsCheckBox.isChecked()) {
            showSnackbar("⚠️ Bạn phải đồng ý điều khoản!", false);
            return;
        }

        if (!validateInput(email, password)) {
            return;
        }

        // Tạo username mặc định từ email (hoặc thêm trường nhập username nếu muốn)
        String username = email.split("@")[0];

        // Show loading state
        continueButton.setEnabled(false);
        continueButton.setText("Đang đăng ký...");

        // Gọi UserDAO để đăng ký
        UserDAO.register(username, email, password, new UserDAO.RegisterCallback() {
            @Override
            public void onSuccess() {
                showSnackbar("✅ Đăng ký thành công!", true);
                new android.os.Handler().postDelayed(() -> finish(), 2000); // Quay lại màn hình đăng nhập sau 2s
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi đăng ký: " + error, false);
                continueButton.setEnabled(true);
                continueButton.setText("Tiếp tục");
            }
        });
    }

    private void goToSignIn() {
        finish(); // Quay lại màn hình đăng nhập
    }

    // Method để hiển thị Snackbar đẹp
    private void showSnackbar(String message, boolean isSuccess) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar;

        if (isSuccess) {
            snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
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
