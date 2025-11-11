package com.example.prm392_cooking_guide_javaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.prm392_cooking_guide_javaapp.dao.DishDAO;
import com.example.prm392_cooking_guide_javaapp.entity.CookingStep;
import com.example.prm392_cooking_guide_javaapp.entity.CookingStepAdapter;
import com.example.prm392_cooking_guide_javaapp.entity.Dish;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DishDetailActivity extends AppCompatActivity {
    private ImageView backButton, favoriteButton, dishImageView;
    private TextView dishNameTextView, dishDescriptionTextView;
    private TextView difficultyTextView, ingredientsTextView;
    private RecyclerView stepsRecyclerView;

    private CookingStepAdapter stepAdapter;
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
        ingredientsTextView = findViewById(R.id.ingredientsTextView);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
    }

    private void setupData() {
        dishId = getIntent().getIntExtra("DISH_ID", 0);
        currentUserId = getIntent().getIntExtra("USER_ID", 0);
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
                showSnackbar("❌ Lỗi tải món ăn: " + error, false);
                finish();
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
        dishNameTextView.setText(dish.getName());
        dishDescriptionTextView.setText(dish.getDescription());
        difficultyTextView.setText(getDifficultyText(dish.getDifficultyLevel()));
        loadDishImage(dish.getImageUrl());
        ingredientsTextView.setText(formatIngredients(dish.getIngredient()));

        List<CookingStep> steps = parseCookingStepsFromDatabase(dish.getCookingSteps());
        stepAdapter = new CookingStepAdapter(steps);
        stepsRecyclerView.setAdapter(stepAdapter);
    }

    private void loadDishImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageUrl);
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

    private String getDifficultyText(String difficulty) {
        if (difficulty == null) return "Chưa xác định";
        switch (difficulty.toLowerCase()) {
            case "easy": return "Dễ làm";
            case "medium": return "Trung bình";
            case "hard": return "Khó";
            default: return difficulty;
        }
    }

    private String formatIngredients(String ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "Chưa có thông tin nguyên liệu";
        }
        String[] items = ingredients.split(",");
        StringBuilder formatted = new StringBuilder();
        for (String item : items) {
            formatted.append("• ").append(item.trim()).append("\n");
        }
        return formatted.toString().trim();
    }

    private List<CookingStep> parseCookingStepsFromDatabase(String cookingSteps) {
        List<CookingStep> steps = new ArrayList<>();
        if (cookingSteps == null || cookingSteps.trim().isEmpty()) {
            steps.add(new CookingStep(1, "Bước 1", "Chưa có hướng dẫn chi tiết"));
            return steps;
        }

        cookingSteps = cookingSteps.replace("\\n", "\n");
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

    /** ✅ Toggle favorite và gửi kết quả về cho màn trước để reload */
    private void toggleFavorite() {
        DishDAO.toggleFavorite(currentUserId, dishId, new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                isFavorite = !isFavorite;
                updateFavoriteButton();

                String actionMessage = isFavorite
                        ? "❤️ Đã thêm vào yêu thích"
                        : "💔 Đã xóa khỏi yêu thích";
                showSnackbar(actionMessage, true);

                // ✅ Gửi tín hiệu về cho màn trước
                Intent resultIntent = new Intent();
                resultIntent.putExtra("FAVORITES_UPDATED", true);
                setResult(RESULT_OK, resultIntent);
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
