package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.example.myapplication.entity.CookingStepAdapter;
import com.example.myapplication.R;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import com.example.myapplication.entity.CookingStep;
import com.example.myapplication.entity.DishIngredient;
import com.example.myapplication.entity.IngredientAdapter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DishDetailActivity extends AppCompatActivity {
    private ImageView backButton, favoriteButton, dishImageView;
    private TextView dishNameTextView, dishDescriptionTextView;
    private TextView difficultyTextView;
    private RecyclerView ingredientsRecyclerView;
    private TextView ingredientsEmptyTextView;
    private RecyclerView stepsRecyclerView;

    private TextView tvTotalCalories;
    private CookingStepAdapter stepAdapter;
    private IngredientAdapter ingredientAdapter;
    private final List<DishIngredient> ingredientList = new ArrayList<>();
    private int dishId;
    private int currentUserId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        initViews();
        setupData();
        setupClickListeners();
        loadDishDetails();
        checkFavoriteStatus();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        dishImageView = findViewById(R.id.dishImageView);
        dishNameTextView = findViewById(R.id.dishNameTextView);
        dishDescriptionTextView = findViewById(R.id.dishDescriptionTextView);
        difficultyTextView = findViewById(R.id.difficultyTextView);
        tvTotalCalories = findViewById(R.id.tv_total_calories);
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        ingredientsEmptyTextView = findViewById(R.id.ingredientsEmptyTextView);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);

        // Setup ingredients list
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsRecyclerView.setNestedScrollingEnabled(false);
        ingredientAdapter = new IngredientAdapter(ingredientList);
        ingredientsRecyclerView.setAdapter(ingredientAdapter);
    }

    private void setupData() {
        dishId = getIntent().getIntExtra("DISH_ID", 0);
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        // Setup RecyclerView
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    private void loadDishDetails() {
        DishDAO.getDishById(dishId, new DishDAO.DishDetailCallback() {
            @Override
            public void onSuccess(Dish dish) {
                displayDishDetails(dish);
            }

            @Override
            public void onError(String error) {
                Log.e("DishDetailActivity", "Error loading dish: " + error);
                showSnackbar("❌ Lỗi tải món ăn: " + error, false);
                // Don't auto-close - let user see the error and go back manually
                // finish();
            }
        });
    }

    private void checkFavoriteStatus() {
        DishDAO.checkIfFavorite(currentUserId, dishId, isFavorite -> {
            this.isFavorite = isFavorite;
            updateFavoriteButton();
        });
    }

    private void displayDishDetails(Dish dish) {
        // Hiển thị thông tin cơ bản
        dishNameTextView.setText(dish.getName());
        dishDescriptionTextView.setText(dish.getDescription());
        difficultyTextView.setText(getDifficultyText(dish.getDifficultyLevel()));
        tvTotalCalories.setText("Tổng: " + dish.getTotalCalories() + " cal");
        // THÊM ĐOẠN CODE NÀY ĐỂ HIỂN THỊ ẢNH MÓN ĂN
        loadDishImage(dish.getImageUrl());

        // Load ingredients list with calories & quantity
        loadDishIngredients(dish.getId());

        // Parse cooking_steps từ database và hiển thị
        List<CookingStep> steps = parseCookingStepsFromDatabase(dish.getCookingSteps());
        stepAdapter = new CookingStepAdapter(steps);
        stepsRecyclerView.setAdapter(stepAdapter);
    }

    private void loadDishIngredients(int dishId) {
        DishDAO.getIngredientsByDishId(dishId, new DishDAO.DishIngredientsCallback() {
            @Override
            public void onSuccess(List<DishIngredient> ingredients) {
                ingredientList.clear();
                if (ingredients != null) {
                    ingredientList.addAll(ingredients);
                }
                ingredientAdapter.notifyDataSetChanged();

                if (ingredientList.isEmpty()) {
                    ingredientsRecyclerView.setVisibility(View.GONE);
                    ingredientsEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    ingredientsRecyclerView.setVisibility(View.VISIBLE);
                    ingredientsEmptyTextView.setVisibility(View.GONE);

                    int totalCalories = 0;
                    for (DishIngredient ingredient : ingredientList) {
                        totalCalories += ingredient.getCaloriesPerUnit();
                    }
                    if (totalCalories > 0) {
                        tvTotalCalories.setText("Tổng: " + totalCalories + " cal");
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e("DishDetailActivity", "Error loading ingredients: " + error);
                ingredientsRecyclerView.setVisibility(View.GONE);
                ingredientsEmptyTextView.setVisibility(View.VISIBLE);
                ingredientsEmptyTextView.setText("Không thể tải danh sách nguyên liệu");
                showSnackbar("❌ Lỗi tải nguyên liệu: " + error, false);
            }
        });
    }

    private void loadDishImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
            return;
        }

        // Check if it's a URL (http:// or https://)
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // Load image from URL in background thread
            loadImageFromUrl(imageUrl);
        } else {
            // Load image from local file path
            loadImageFromFile(imageUrl);
        }
    }

    private void loadImageFromFile(String filePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap != null) {
                dishImageView.setImageBitmap(bitmap);
            } else {
                dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                Log.w("DishDetailActivity", "Failed to decode image from file: " + filePath);
            }
        } catch (Exception e) {
            dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
            Log.e("DishDetailActivity", "Error loading image from file: " + e.getMessage());
        }
    }

    private void loadImageFromUrl(String imageUrl) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000); // 10 seconds timeout
                connection.setReadTimeout(10000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    connection.disconnect();

                    if (bitmap != null) {
                        // Update UI on main thread
                        handler.post(() -> {
                            dishImageView.setImageBitmap(bitmap);
                        });
                    } else {
                        handler.post(() -> {
                            dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                            Log.w("DishDetailActivity", "Failed to decode bitmap from URL: " + imageUrl);
                        });
                    }
                } else {
                    handler.post(() -> {
                        dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                        Log.w("DishDetailActivity", "HTTP error " + responseCode + " for URL: " + imageUrl);
                    });
                    connection.disconnect();
                }
            } catch (Exception e) {
                handler.post(() -> {
                    dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                    Log.e("DishDetailActivity", "Error loading image from URL: " + e.getMessage());
                });
            }
        });
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

    /**
     * Parse cooking_steps từ cột cooking_steps trong bảng Dishes
     * Format trong database: "1. Boil pasta\n2. Cook pancetta\n3. Mix with eggs and cheese"
     */
    private List<CookingStep> parseCookingStepsFromDatabase(String cookingSteps) {
        List<CookingStep> steps = new ArrayList<>();

        if (cookingSteps == null || cookingSteps.trim().isEmpty()) {
            steps.add(new CookingStep(1, "Bước 1", "Chưa có hướng dẫn chi tiết"));
            return steps;
        }

        // Chuyển tất cả '\\n' thành xuống dòng thực sự '\n'
        cookingSteps = cookingSteps.replace("\\n", "\n");

        // Dùng regex để tách theo định dạng số thứ tự đầu dòng
        Pattern pattern = Pattern.compile("(?m)^(\\d+)\\.\\s*(.*?)(?=^\\d+\\.|\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cookingSteps);

        int stepNumber = 1;
        while (matcher.find()) {
            String content = matcher.group(2).trim();
            if (!content.isEmpty()) {
                steps.add(new CookingStep(stepNumber, "Bước " + stepNumber, content));
                stepNumber++;
            }
        }

        // Nếu không match theo regex, fallback tách từng dòng
        if (steps.isEmpty()) {
            String[] lines = cookingSteps.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].replaceFirst("^\\d+\\.\\s*", "").trim();
                if (!line.isEmpty()) {
                    steps.add(new CookingStep(i + 1, "Bước " + (i + 1), line));
                }
            }
        }

        return steps;
    }

    private void toggleFavorite() {
        DishDAO.toggleFavorite(currentUserId, dishId, new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                isFavorite = !isFavorite;
                updateFavoriteButton();
                String actionMessage = isFavorite ? "❤️ Đã thêm vào yêu thích" : "💔 Đã xóa khỏi yêu thích";
                showSnackbar(actionMessage, true);
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi thao tác yêu thích: " + error, false);
            }
        });
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_bookmark_filled);
            favoriteButton.setColorFilter(getColor(android.R.color.holo_red_light));
        } else {
            favoriteButton.setImageResource(R.drawable.ic_bookmark_border);
            favoriteButton.setColorFilter(getColor(android.R.color.darker_gray));
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
