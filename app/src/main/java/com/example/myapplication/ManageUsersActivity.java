package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.example.myapplication.entity.AdminUserAdapter; // Sửa import
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private AdminUserAdapter userAdapter;
    private List<User> usersList;
    private ImageView backButton;

    // THÊM CONSTANT CHO REQUEST CODE
    private static final int EDIT_USER_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        backButton = findViewById(R.id.backButton);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersList = new ArrayList<>();

        userAdapter = new AdminUserAdapter(usersList, this::onEditUser, this::onDeleteUser);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        backButton.setOnClickListener(v -> finish());
        loadAllUsers();
    }

    private void loadAllUsers() {
        UserDAO.getAllUsers(new UserDAO.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                usersList.clear();
                usersList.addAll(users);
                userAdapter.notifyDataSetChanged();

                if (users.isEmpty()) {
                    showSnackbar("📋 Chưa có người dùng nào trong hệ thống", false);
                }
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi tải danh sách người dùng: " + error, false);
            }
        });
    }

    // SỬA METHOD NÀY - THAY startActivity BẰNG startActivityForResult
    private void onEditUser(User user) {
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("USER_ID", user.getId());
        startActivityForResult(intent, EDIT_USER_REQUEST); // Thay đổi ở đây
    }

    // THÊM METHOD NÀY ĐỂ XỬ LÝ KẾT QUẢ TRẢ VỀ
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_USER_REQUEST && resultCode == RESULT_OK) {
            // Reload danh sách users sau khi chỉnh sửa thành công
            loadAllUsers();
            showSnackbar("✅ Đã cập nhật thông tin người dùng", true);
        }
    }

    private void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa người dùng " + user.getUsername() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    UserDAO.deleteUser(user.getId(), new UserDAO.DeleteCallback() {
                        @Override
                        public void onSuccess() {
                            showSnackbar("🗑️ Đã xóa người dùng thành công", true);
                            loadAllUsers();
                        }

                        @Override
                        public void onError(String error) {
                            showSnackbar("❌ Lỗi xóa người dùng: " + error, false);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
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
