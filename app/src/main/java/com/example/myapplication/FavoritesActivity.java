package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView; // 🔹 Thêm import
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.entity.DishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements DishAdapter.FavoriteChecker {
    private RecyclerView favoritesRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> favoritesList;
    private int currentUserId;
    private ImageView backButton; // 🔹 Thêm biến nút back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initViews();
        setupData();
        loadFavorites();
    }

    private void initViews() {
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        backButton = findViewById(R.id.backButton); // 🔹 Ánh xạ nút back

        // 🔹 Xử lý sự kiện click để quay lại
        backButton.setOnClickListener(v -> finish());
    }

    private void setupData() {
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        favoritesList = new ArrayList<>();

        dishAdapter = new DishAdapter(favoritesList, this::onDishClick, this::onFavoriteClick, this);

        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(dishAdapter);
    }

    @Override
    public boolean isDishFavorite(int dishId) {
        return true; // Vì tất cả món trong Favorites đều là favorite
    }

    private void loadFavorites() {
        DishDAO.getFavoriteDishes(currentUserId, new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                favoritesList.clear();
                favoritesList.addAll(dishes);
                dishAdapter.notifyDataSetChanged();

                if (dishes.isEmpty()) {
                    showSnackbar("💔 Chưa có món ăn yêu thích nào", false);
                }
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi tải danh sách yêu thích: " + error, false);
            }
        });
    }

    private void onDishClick(Dish dish) {
        Intent intent = new Intent(this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", dish.getId());
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    private void onFavoriteClick(Dish dish) {
        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                showSnackbar("💔 Đã xóa khỏi danh sách yêu thích", true);
                loadFavorites();
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi xóa khỏi yêu thích: " + error, false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

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
