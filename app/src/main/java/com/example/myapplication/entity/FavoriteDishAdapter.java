package com.example.myapplication.entity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.DishDetailActivity;
import com.example.myapplication.R;
import com.example.myapplication.entity.Dish;
import java.util.List;

public class FavoriteDishAdapter extends RecyclerView.Adapter<FavoriteDishAdapter.FavoriteDishViewHolder> {
    private List<Dish> dishes;
    private Context context;
    private int currentUserId;
    private OnDishClickListener clickListener;

    // Interface cho click listener
    public interface OnDishClickListener {
        void onDishClick(Dish dish);
    }

    // Constructor với click listener
    public FavoriteDishAdapter(List<Dish> dishes, Context context, int currentUserId, OnDishClickListener clickListener) {
        this.dishes = dishes;
        this.context = context;
        this.currentUserId = currentUserId;
        this.clickListener = clickListener;
    }

    // Constructor cũ (backward compatibility)
    public FavoriteDishAdapter(List<Dish> dishes, Context context, int currentUserId) {
        this.dishes = dishes;
        this.context = context;
        this.currentUserId = currentUserId;
        this.clickListener = null;
    }

    @NonNull
    @Override
    public FavoriteDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_dish, parent, false);
        return new FavoriteDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteDishViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    class FavoriteDishViewHolder extends RecyclerView.ViewHolder {
        private ImageView dishImageView;
        private TextView dishNameTextView;

        public FavoriteDishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishImageView = itemView.findViewById(R.id.dishImageView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTextView);
        }

        public void bind(Dish dish) {
            dishNameTextView.setText(dish.getName());

            // Load ảnh từ database
            loadDishImage(dish.getImageUrl());

            // Thiết lập click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onDishClick(dish);
                } else {
                    // Fallback: chuyển trực tiếp sang DishDetailActivity
                    Intent intent = new Intent(context, DishDetailActivity.class);
                    intent.putExtra("DISH_ID", dish.getId());
                    intent.putExtra("USER_ID", currentUserId);
                    context.startActivity(intent);
                }
            });
        }

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
    }
}
