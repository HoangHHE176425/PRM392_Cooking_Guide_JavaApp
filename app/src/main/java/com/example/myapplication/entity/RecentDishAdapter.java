package com.example.myapplication.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.List;

public class RecentDishAdapter extends RecyclerView.Adapter<RecentDishAdapter.RecentDishViewHolder> {
    private List<RecentDish> recentDishes;
    private OnRecentDishClickListener listener;

    public interface OnRecentDishClickListener {
        void onRecentDishClick(RecentDish recentDish);
    }

    public RecentDishAdapter(List<RecentDish> recentDishes, OnRecentDishClickListener listener) {
        this.recentDishes = recentDishes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecentDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_dish, parent, false);
        return new RecentDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentDishViewHolder holder, int position) {
        RecentDish recentDish = recentDishes.get(position);
        holder.bind(recentDish);
    }

    @Override
    public int getItemCount() {
        return recentDishes.size();
    }

    class RecentDishViewHolder extends RecyclerView.ViewHolder {
        private TextView dishNameTextView, timeAgoTextView;
        private ImageView dishImageView; // Thêm ImageView


        public RecentDishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTextView);
            timeAgoTextView = itemView.findViewById(R.id.timeAgoTextView);
            dishImageView = itemView.findViewById(R.id.dishImageView); // Ánh xạ ImageView

        }

        public void bind(RecentDish recentDish) {
            dishNameTextView.setText(recentDish.getName());
            timeAgoTextView.setText(recentDish.getTimeAgo());

            // Load ảnh từ imageUrl
            loadDishImage(recentDish.getImageUrl());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecentDishClick(recentDish);
                }
            });
        }

        private void loadDishImage(String imageUrl) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Decode ảnh từ đường dẫn local
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageUrl);
                    if (bitmap != null) {
                        dishImageView.setImageBitmap(bitmap);
                    } else {
                        // Hiển thị ảnh mặc định nếu không load được
                        dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                    }
                } catch (Exception e) {
                    // Hiển thị ảnh mặc định nếu có lỗi
                    dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                }
            } else {
                // Hiển thị ảnh mặc định nếu không có URL
                dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
            }
        }
    }
}
