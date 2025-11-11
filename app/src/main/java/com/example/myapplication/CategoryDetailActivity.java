package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.DishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDetailActivity extends AppCompatActivity implements DishAdapter.FavoriteChecker {
    private TextView categoryTitleTextView;
    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> dishList;
    private int currentUserId;

    // Thêm cache favorite
    private Map<Integer, Boolean> favoriteCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Lấy USER_ID từ Intent
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        initViews();
        setupData();
        loadDishes();
    }

    private void initViews() {
        categoryTitleTextView = findViewById(R.id.categoryTitleTextView);
        dishRecyclerView = findViewById(R.id.dishRecyclerView);
    }

    private void setupData() {
        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        categoryTitleTextView.setText(categoryName);

        dishList = new ArrayList<>();

        // Sửa constructor - thêm this làm FavoriteChecker
        dishAdapter = new DishAdapter(dishList, this::onDishClick, this::onFavoriteClick, this);

        dishRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dishRecyclerView.setAdapter(dishAdapter);
    }

    private void loadDishes() {
        DishDAO.getAllDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                dishList.clear();
                dishList.addAll(dishes);

                // Load favorite status sau khi có danh sách
                loadFavoriteStatus();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CategoryDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thêm method load favorite status
    private void loadFavoriteStatus() {
        if (dishList.isEmpty()) {
            dishAdapter.notifyDataSetChanged();
            return;
        }

        // Khởi tạo tất cả là false
        favoriteCache.clear();
        for (Dish dish : dishList) {
            favoriteCache.put(dish.getId(), false);
        }

        // Kiểm tra favorite status cho từng món ăn
        final int[] checkedCount = {0};
        final int totalCount = dishList.size();

        for (Dish dish : dishList) {
            DishDAO.checkIfFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCheckCallback() {
                @Override
                public void onResult(boolean isFavorite) {
                    favoriteCache.put(dish.getId(), isFavorite);
                    checkedCount[0]++;

                    // Khi đã check xong tất cả
                    if (checkedCount[0] == totalCount) {
                        runOnUiThread(() -> {
                            dishAdapter.notifyDataSetChanged();
                        });
                    }
                }
            });
        }
    }

    // Implement interface FavoriteChecker
    @Override
    public boolean isDishFavorite(int dishId) {
        return favoriteCache.getOrDefault(dishId, false);
    }

    private void onDishClick(Dish dish) {
        Toast.makeText(this, "Clicked: " + dish.getName(), Toast.LENGTH_SHORT).show();
        // Hoặc mở DishDetailActivity
        // Intent intent = new Intent(this, DishDetailActivity.class);
        // intent.putExtra("DISH_ID", dish.getId());
        // intent.putExtra("USER_ID", currentUserId);
        // startActivity(intent);
    }

    private void onFavoriteClick(Dish dish) {
        // Cập nhật cache ngay lập tức
        boolean currentStatus = favoriteCache.getOrDefault(dish.getId(), false);
        favoriteCache.put(dish.getId(), !currentStatus);
        dishAdapter.notifyDataSetChanged();

        // Toggle favorite trong database
        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(CategoryDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                // Revert cache nếu có lỗi
                favoriteCache.put(dish.getId(), currentStatus);
                dishAdapter.notifyDataSetChanged();
                Toast.makeText(CategoryDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
