package com.example.prm392_cooking_guide_javaapp.entity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_cooking_guide_javaapp.R;

import com.example.prm392_cooking_guide_javaapp.entity.IngredientAdapter;
import com.example.prm392_cooking_guide_javaapp.entity.DishDAO;
import com.example.prm392_cooking_guide_javaapp.entity.DishDetail;
import com.example.prm392_cooking_guide_javaapp.entity.DishIngredient;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DishDetailActivity extends AppCompatActivity {
    private static final String TAG = "DishDetailActivity";

    // Views
    private ImageView ivDishImage;
    private TextView tvDishName;
    private TextView tvDescription;
    private TextView tvDifficulty;
    private TextView tvTotalCalories;
    private TextView tvCookingSteps;
    private RecyclerView rvIngredients;
    private ProgressBar progressBar;

    // Data
    private int dishId;
    private DishDAO dishDAO;
    private IngredientAdapter ingredientAdapter;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        // Nhận dishId từ Intent
        dishId = getIntent().getIntExtra("DISH_ID", -1);
        if (dishId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy món ăn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();

        dishDAO = new DishDAO();
        executorService = Executors.newSingleThreadExecutor();

        loadDishDetails();
    }

    private void initViews() {
        ivDishImage = findViewById(R.id.iv_dish_image);
        tvDishName = findViewById(R.id.tv_dish_name);
        tvDescription = findViewById(R.id.tv_description);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        tvTotalCalories = findViewById(R.id.tv_total_calories);
        tvCookingSteps = findViewById(R.id.tv_cooking_steps);
        rvIngredients = findViewById(R.id.rv_ingredients);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        ingredientAdapter = new IngredientAdapter(new ArrayList<>());
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(ingredientAdapter);
    }

    private void loadDishDetails() {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            try {
                // Lấy thông tin món ăn
                DishDetail dishDetail = dishDAO.getDishDetailById(dishId);

                // Lấy danh sách nguyên liệu
                List<DishIngredient> ingredients = dishDAO.getIngredientsByDishId(dishId);

                // Cập nhật UI trên Main Thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (dishDetail != null) {
                        displayDishDetail(dishDetail);
                    } else {
                        Toast.makeText(this, "Không tìm thấy món ăn",
                                Toast.LENGTH_SHORT).show();
                    }

                    if (ingredients != null && !ingredients.isEmpty()) {
                        ingredientAdapter.updateData(ingredients);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, " Lỗi khi load chi tiết món ăn: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayDishDetail(DishDetail dish) {
        tvDishName.setText(dish.getName());
        tvDescription.setText(dish.getDescription());
        tvDifficulty.setText("Độ khó: " + dish.getDifficultyLevel());
        tvTotalCalories.setText("Tổng: " + dish.getTotalCalories() + " cal");
        tvCookingSteps.setText(dish.getCookingSteps().replace("\\n", "\n"));

        // Load ảnh bằng Picasso (thêm dependency trong build.gradle)
        if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(dish.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_add_dish)
                    .into(ivDishImage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}