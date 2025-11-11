package com.example.myapplication.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.entity.User;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private List<User> usersList;
    private OnUserActionListener editListener;
    private OnUserActionListener deleteListener;

    public interface OnUserActionListener {
        void onAction(User user);
    }

    public AdminUserAdapter(List<User> usersList, OnUserActionListener editListener, OnUserActionListener deleteListener) {
        this.usersList = usersList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTextView, emailTextView, fullNameTextView, roleTextView;
        private Button editButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            fullNameTextView = itemView.findViewById(R.id.fullNameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(User user) {
            usernameTextView.setText("@" + user.getUsername());
            emailTextView.setText(user.getEmail());
            fullNameTextView.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
            roleTextView.setText(user.getRole().toUpperCase());

            // Đổi màu role
            if ("admin".equals(user.getRole())) {
                roleTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            } else {
                roleTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
            }

            editButton.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onAction(user);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onAction(user);
                }
            });
        }
    }
}
