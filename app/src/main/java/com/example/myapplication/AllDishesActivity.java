package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.example.myapplication.adapter.DishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllDishesActivity extends AppCompatActivity implements DishAdapter.FavoriteChecker {
    private EditText searchEditText;
    private ChipGroup difficultyChipGroup;
    private RecyclerView dishRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyTextView;

    private DishAdapter dishAdapter;
    private List<Dish> allDishes = new ArrayList<>();
    private List<Dish> filteredDishes = new ArrayList<>();
    private int currentUserId;

    // THÊM FAVORITE CACHE
    private Map<Integer, Boolean> favoriteCache = new HashMap<>();

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private boolean isLoading = false;

    // Filter
    private String searchQuery = "";
    private String selectedDifficulty = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_dishes);

        initViews();
        setupRecyclerView();
        setupFilters();
        loadDishes();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        difficultyChipGroup = findViewById(R.id.difficultyChipGroup);
        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        currentUserId = getIntent().getIntExtra("USER_ID", 0);
    }

    private void setupRecyclerView() {
        // TRUYỀN this làm FavoriteChecker
        dishAdapter = new DishAdapter(filteredDishes, this::onDishClick, this::onFavoriteClick, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dishRecyclerView.setLayoutManager(layoutManager);
        dishRecyclerView.setAdapter(dishAdapter);

        // Pagination scroll listener
        dishRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= itemsPerPage) {
                        loadMoreDishes();
                    }
                }
            }
        });
    }

    private void setupFilters() {
        // Search filter
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Difficulty filter
        difficultyChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedDifficulty = "";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedDifficulty = selectedChip.getText().toString().toLowerCase();
            }
            applyFilters();
        });
    }

    private void loadDishes() {
        showLoading(true);
        DishDAO.getAllDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                allDishes.clear();
                allDishes.addAll(dishes);

                // LOAD FAVORITE STATUS SAU KHI CÓ DANH SÁCH
                loadFavoriteStatus();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                showSnackbar("❌ Lỗi tải danh sách món ăn: " + error, false);
            }
        });
    }

    // THÊM METHOD LOAD FAVORITE STATUS
    private void loadFavoriteStatus() {
        if (allDishes.isEmpty()) {
            showLoading(false);
            applyFilters();
            return;
        }

        // Khởi tạo tất cả dishes là chưa favorite
        favoriteCache.clear();
        for (Dish dish : allDishes) {
            favoriteCache.put(dish.getId(), false);
        }

        // Đếm số món ăn đã kiểm tra
        final int[] checkedCount = {0};
        final int totalCount = allDishes.size();

        for (Dish dish : allDishes) {
            DishDAO.checkIfFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCheckCallback() {
                @Override
                public void onResult(boolean isFavorite) {
                    favoriteCache.put(dish.getId(), isFavorite);
                    checkedCount[0]++;

                    // Khi đã kiểm tra xong tất cả
                    if (checkedCount[0] == totalCount) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            applyFilters();
                        });
                    }
                }
            });
        }
    }

    // IMPLEMENT INTERFACE FavoriteChecker
    @Override
    public boolean isDishFavorite(int dishId) {
        return favoriteCache.getOrDefault(dishId, false);
    }

    private void loadMoreDishes() {
        if (isLoading) return;

        isLoading = true;
        currentPage++;

        new android.os.Handler().postDelayed(() -> {
            int startIndex = (currentPage - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, getFilteredResults().size());

            if (startIndex < getFilteredResults().size()) {
                List<Dish> newItems = getFilteredResults().subList(startIndex, endIndex);
                filteredDishes.addAll(newItems);
                dishAdapter.notifyItemRangeInserted(filteredDishes.size() - newItems.size(), newItems.size());
            }

            isLoading = false;
        }, 1000);
    }

    private void applyFilters() {
        List<Dish> results = getFilteredResults();

        currentPage = 1;
        filteredDishes.clear();

        int endIndex = Math.min(itemsPerPage, results.size());
        if (endIndex > 0) {
            filteredDishes.addAll(results.subList(0, endIndex));
        }

        dishAdapter.notifyDataSetChanged();

        if (filteredDishes.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            dishRecyclerView.setVisibility(View.GONE);
            // CHỈ hiển thị thông báo khi không tìm thấy kết quả
            if (!searchQuery.isEmpty() || !selectedDifficulty.isEmpty()) {
                showSnackbar("🔍 Không tìm thấy món ăn phù hợp với bộ lọc", false);
            }
        } else {
            emptyTextView.setVisibility(View.GONE);
            dishRecyclerView.setVisibility(View.VISIBLE);
            // XÓA thông báo khi filter thành công
        }
    }

    private List<Dish> getFilteredResults() {
        List<Dish> results = new ArrayList<>();

        for (Dish dish : allDishes) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    dish.getName().toLowerCase().contains(searchQuery) ||
                    dish.getDescription().toLowerCase().contains(searchQuery);

            boolean matchesDifficulty = selectedDifficulty.isEmpty() ||
                    dish.getDifficultyLevel().toLowerCase().equals(selectedDifficulty);

            if (matchesSearch && matchesDifficulty) {
                results.add(dish);
            }
        }

        return results;
    }

    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        dishRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void onDishClick(Dish dish) {
        Intent intent = new Intent(this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", dish.getId());
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    private void onFavoriteClick(Dish dish) {
        // Cập nhật cache ngay lập tức
        boolean currentStatus = favoriteCache.getOrDefault(dish.getId(), false);
        favoriteCache.put(dish.getId(), !currentStatus);
        dishAdapter.notifyDataSetChanged();

        String actionMessage = !currentStatus ? "❤️ Đã thêm vào yêu thích" : "💔 Đã xóa khỏi yêu thích";

        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                showSnackbar(actionMessage, true);
            }

            @Override
            public void onError(String error) {
                // Revert cache nếu có lỗi
                favoriteCache.put(dish.getId(), currentStatus);
                dishAdapter.notifyDataSetChanged();
                showSnackbar("❌ Lỗi thao tác yêu thích: " + error, false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorite status khi quay lại từ DishDetailActivity
        if (!allDishes.isEmpty()) {
            loadFavoriteStatus();
        }
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
