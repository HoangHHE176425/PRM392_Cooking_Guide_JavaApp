package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.io.FileOutputStream;

public class EditDishActivity extends AppCompatActivity {
    private EditText dishNameEditText, dishDescriptionEditText, ingredientsEditText, cookingStepsEditText;
    private Spinner difficultySpinner;
    private ImageView dishImageView, backButton;
    private Button selectImageButton, saveDishButton;
    private String dishImagePath;
    private int dishId;
    private Dish currentDish;
    private static final int REQUEST_PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dish);

        dishId = getIntent().getIntExtra("DISH_ID", 0);

        initViews();
        setupClickListeners();
        loadDishData();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        dishNameEditText = findViewById(R.id.dishNameEditText);
        dishDescriptionEditText = findViewById(R.id.dishDescriptionEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        cookingStepsEditText = findViewById(R.id.cookingStepsEditText);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        dishImageView = findViewById(R.id.dishImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveDishButton = findViewById(R.id.saveDishButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveDishButton.setOnClickListener(v -> saveDish());
    }

    private void loadDishData() {
        if (dishId <= 0) {
            showSnackbar("❌ ID món ăn không hợp lệ", false);
            new android.os.Handler().postDelayed(() -> finish(), 2000);
            return;
        }

        DishDAO.getDishById(dishId, new DishDAO.DishDetailCallback() {
            @Override
            public void onSuccess(Dish dish) {
                if (dish != null) {
                    currentDish = dish;
                    dishNameEditText.setText(dish.getName() != null ? dish.getName() : "");
                    dishDescriptionEditText.setText(dish.getDescription() != null ? dish.getDescription() : "");
                    ingredientsEditText.setText(dish.getIngredient() != null ? dish.getIngredient() : "");
                    cookingStepsEditText.setText(dish.getCookingSteps() != null ? dish.getCookingSteps() : "");

                    // Set difficulty spinner
                    String[] difficulties = {"easy", "medium", "hard"};
                    String currentDifficulty = dish.getDifficultyLevel();
                    if (currentDifficulty != null) {
                        for (int i = 0; i < difficulties.length; i++) {
                            if (difficulties[i].equalsIgnoreCase(currentDifficulty)) {
                                difficultySpinner.setSelection(i);
                                break;
                            }
                        }
                    }

                    // Load image
                    if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
                        try {
                            Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(dish.getImageUrl());
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
                } else {
                    showSnackbar("❌ Không tìm thấy thông tin món ăn", false);
                    new android.os.Handler().postDelayed(() -> finish(), 2000);
                }
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi tải dữ liệu món ăn: " + error, false);
                new android.os.Handler().postDelayed(() -> finish(), 2000);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh món ăn"), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                dishImageView.setImageBitmap(bitmap);
                dishImagePath = saveDishImage(bitmap, "dish_" + dishId + "_" + System.currentTimeMillis() + ".png");
                showSnackbar("📷 Đã chọn ảnh mới", true);
            } catch (Exception e) {
                showSnackbar("❌ Lỗi chọn ảnh", false);
            }
        }
    }

    private void saveDish() {
        String name = dishNameEditText.getText().toString().trim();
        String description = dishDescriptionEditText.getText().toString().trim();
        String ingredients = ingredientsEditText.getText().toString().trim();
        String cookingSteps = cookingStepsEditText.getText().toString().trim();
        String difficulty = difficultySpinner.getSelectedItem().toString();

        if (name.isEmpty() || description.isEmpty() || ingredients.isEmpty() || cookingSteps.isEmpty()) {
            showSnackbar("⚠️ Vui lòng nhập đầy đủ thông tin", false);
            return;
        }

        Dish updatedDish = new Dish();
        updatedDish.setId(currentDish.getId());
        updatedDish.setName(name);
        updatedDish.setDescription(description);
        updatedDish.setIngredient(ingredients);
        updatedDish.setCookingSteps(cookingSteps);
        updatedDish.setDifficultyLevel(difficulty);
        updatedDish.setUserId(currentDish.getUserId());
        updatedDish.setCreatedAt(currentDish.getCreatedAt());

        // Xử lý ảnh
        if (dishImagePath != null && !dishImagePath.isEmpty()) {
            updatedDish.setImageUrl(dishImagePath);
        } else {
            updatedDish.setImageUrl(currentDish.getImageUrl());
        }

        // Show loading state
        saveDishButton.setEnabled(false);
        saveDishButton.setText("Đang cập nhật...");

        DishDAO.updateDish(updatedDish, new DishDAO.AddDishCallback() {
            @Override
            public void onSuccess() {
                showSnackbar("✅ Cập nhật món ăn thành công", true);
                new android.os.Handler().postDelayed(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, 1500);
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi cập nhật món ăn: " + error, false);
                saveDishButton.setEnabled(true);
                saveDishButton.setText("Lưu thay đổi");
            }
        });
    }

    private String saveDishImage(Bitmap bitmap, String filename) {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return getFilesDir() + "/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method để hiển thị Snackbar đẹp
    private void showSnackbar(String message, boolean isSuccess) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar;

        if (isSuccess) {
            snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
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
