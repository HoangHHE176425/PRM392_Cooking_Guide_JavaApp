package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;

import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private TextView signUpLinkTextView;
    private CoordinatorLayout coordinatorLayout; // Thêm để làm container cho Snackbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout); // Thêm dòng này
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpLinkTextView = findViewById(R.id.signUpLinkTextView);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> performSignIn());
        signUpLinkTextView.setOnClickListener(v -> goToSignUp());
    }

    private void performSignIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            // Thay Toast bằng Snackbar với màu đỏ cho error
            showErrorSnackbar("Please fill all fields");
            return;
        }

        // Hiển thị loading
        signInButton.setEnabled(false);
        signInButton.setText("Signing in...");

        UserDAO.login(email, password, new UserDAO.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");

                // Hiển thị thông báo thành công
                showSuccessSnackbar("Welcome back, " + user.getUsername() + "!");

                // Delay một chút để user thấy thông báo thành công
                coordinatorLayout.postDelayed(() -> {
                    if ("admin".equals(user.getRole())) {
                        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("USER_ID", user.getId());
                        intent.putExtra("USERNAME", user.getUsername());
                        intent.putExtra("EMAIL", user.getEmail());
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.putExtra("USER_ID", user.getId());
                        intent.putExtra("USERNAME", user.getUsername());
                        intent.putExtra("EMAIL", user.getEmail());
                        intent.putExtra("FULL_NAME", user.getFullName());
                        intent.putExtra("BIO", user.getBio());
                        startActivity(intent);
                        finish();
                    }
                }, 1000); // Delay 1 giây
            }

            @Override
            public void onError(String error) {
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");
                showErrorSnackbar(error);
            }
        });
    }

    private void goToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    // Thêm các method để hiển thị Snackbar đẹp
    private void showSuccessSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
        snackbar.setTextColor(getResources().getColor(android.R.color.white));
        snackbar.show();
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "❌ " + message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
        snackbar.setTextColor(getResources().getColor(android.R.color.white));
        snackbar.setAction("DISMISS", v -> snackbar.dismiss());
        snackbar.setActionTextColor(getResources().getColor(android.R.color.white));
        snackbar.show();
    }
}
