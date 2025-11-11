package com.example.myapplication.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.entity.Dish;
import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {
    private List<Dish> dishes;
    private OnDishClickListener dishClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private FavoriteChecker favoriteChecker;

    public interface OnDishClickListener {
        void onDishClick(Dish dish);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Dish dish);
    }

    public interface FavoriteChecker {
        boolean isDishFavorite(int dishId);
    }

    public DishAdapter(List<Dish> dishes, OnDishClickListener dishClickListener,
                       OnFavoriteClickListener favoriteClickListener, FavoriteChecker favoriteChecker) {
        this.dishes = dishes;
        this.dishClickListener = dishClickListener;
        this.favoriteClickListener = favoriteClickListener;
        this.favoriteChecker = favoriteChecker;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        private TextView dishNameTextView, dishDescriptionTextView, difficultyTextView;
        private ImageView favoriteImageView, dishImageView; // THÊM dishImageView

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTextView);
            dishDescriptionTextView = itemView.findViewById(R.id.dishDescriptionTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            favoriteImageView = itemView.findViewById(R.id.favoriteImageView);
            dishImageView = itemView.findViewById(R.id.dishImageView); // THÊM ánh xạ
        }

        public void bind(Dish dish) {
            dishNameTextView.setText(dish.getName());
            dishDescriptionTextView.setText(dish.getDescription());
            difficultyTextView.setText(getDifficultyText(dish.getDifficultyLevel()));

            // THÊM: Load ảnh món ăn
            loadDishImage(dish.getImageUrl());

            // Kiểm tra trạng thái favorite
            boolean isFavorite = favoriteChecker != null && favoriteChecker.isDishFavorite(dish.getId());
            updateFavoriteButton(isFavorite);

            itemView.setOnClickListener(v -> {
                if (dishClickListener != null) {
                    dishClickListener.onDishClick(dish);
                }
            });

            favoriteImageView.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(dish);
                }
            });
        }

        // THÊM method load ảnh
        private void loadDishImage(String imageUrl) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageUrl);
                    if (bitmap != null) {
                        dishImageView.setImageBitmap(bitmap);
                    } else {
                        dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                    }
                } catch (Exception e) {
                    dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                }
            } else {
                dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
            }
        }

        private void updateFavoriteButton(boolean isFavorite) {
            if (isFavorite) {
                favoriteImageView.setImageResource(R.drawable.ic_bookmark_filled);
                favoriteImageView.setColorFilter(itemView.getContext().getColor(android.R.color.holo_red_light));
            } else {
                favoriteImageView.setImageResource(R.drawable.ic_bookmark_border);
                favoriteImageView.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }
        }

        private String getDifficultyText(String difficulty) {
            if (difficulty == null) return "Chưa xác định";
            switch (difficulty.toLowerCase()) {
                case "easy": return "Dễ làm";
                case "medium": return "Trung bình";
                case "hard": return "Khó";
                default: return difficulty;
            }
        }
    }
}
