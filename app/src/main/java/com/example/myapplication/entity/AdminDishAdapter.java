package com.example.myapplication.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.entity.Dish;
import java.util.List;

public class AdminDishAdapter extends RecyclerView.Adapter<AdminDishAdapter.DishViewHolder> {
    private List<Dish> dishesList;
    private OnDishActionListener editListener;
    private OnDishActionListener deleteListener;

    public interface OnDishActionListener {
        void onAction(Dish dish);
    }

    public AdminDishAdapter(List<Dish> dishesList, OnDishActionListener editListener, OnDishActionListener deleteListener) {
        this.dishesList = dishesList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishesList.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishesList.size();
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        private ImageView dishImageView;
        private TextView dishNameTextView, dishDescriptionTextView, difficultyTextView;
        private Button editButton, deleteButton;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImageView = itemView.findViewById(R.id.dishImageView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTextView);
            dishDescriptionTextView = itemView.findViewById(R.id.dishDescriptionTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Dish dish) {
            dishNameTextView.setText(dish.getName());
            dishDescriptionTextView.setText(dish.getDescription());
            difficultyTextView.setText(dish.getDifficultyLevel().toUpperCase());

            // Load ảnh từ đường dẫn local
            if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(dish.getImageUrl());
                if (bitmap != null) {
                    dishImageView.setImageBitmap(bitmap);
                } else {
                    dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                }
            } else {
                dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
            }

            // Đổi màu difficulty
            switch (dish.getDifficultyLevel().toLowerCase()) {
                case "easy":
                    difficultyTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "medium":
                    difficultyTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "hard":
                    difficultyTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    break;
            }

            editButton.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onAction(dish);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onAction(dish);
                }
            });
        }
    }
}

