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

public class AddDishActivity extends AppCompatActivity {
    private EditText dishNameEditText, dishDescriptionEditText, ingredientsEditText, cookingStepsEditText;
    private Spinner difficultySpinner;
    private ImageView dishImageView;
    private Button selectImageButton, saveDishButton;
    private String dishImagePath;
    private int adminId;
    private static final int REQUEST_PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);

        adminId = getIntent().getIntExtra("ADMIN_ID", 0);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
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
        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveDishButton.setOnClickListener(v -> saveDish());
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
                dishImagePath = saveDishImage(bitmap, "dish_" + System.currentTimeMillis() + ".png");
                showSnackbar("📷 Đã chọn ảnh món ăn", true);
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

        // TỰ ĐỘNG FORMAT COOKING STEPS TỪ XUỐNG DÒNG
        String formattedCookingSteps = formatCookingStepsFromNewLines(cookingSteps);

        Dish newDish = new Dish();
        newDish.setName(name);
        newDish.setDescription(description);
        newDish.setIngredient(ingredients);
        newDish.setCookingSteps(formattedCookingSteps);
        newDish.setDifficultyLevel(difficulty);
        newDish.setUserId(adminId);
        newDish.setImageUrl(dishImagePath);

        // Show loading state
        saveDishButton.setEnabled(false);
        saveDishButton.setText("Đang lưu...");

        DishDAO.addDish(newDish, new DishDAO.AddDishCallback() {
            @Override
            public void onSuccess() {
                showSnackbar("✅ Đã thêm món ăn thành công", true);
                new android.os.Handler().postDelayed(() -> finish(), 1500);
            }

            @Override
            public void onError(String error) {
                showSnackbar("❌ Lỗi thêm món ăn: " + error, false);
                saveDishButton.setEnabled(true);
                saveDishButton.setText("Lưu món ăn");
            }
        });
    }

    private String formatCookingStepsFromNewLines(String rawSteps) {
        if (rawSteps == null || rawSteps.trim().isEmpty()) {
            return rawSteps;
        }

        // Tách các bước bằng xuống dòng
        String[] steps = rawSteps.split("\\n");
        StringBuilder formattedSteps = new StringBuilder();

        int stepNumber = 1;
        for (String step : steps) {
            String trimmedStep = step.trim();
            if (!trimmedStep.isEmpty()) {
                // Loại bỏ số thứ tự cũ nếu có
                trimmedStep = trimmedStep.replaceFirst("^\\d+[\\.\\)]\\s*", "");

                formattedSteps.append(stepNumber).append(". ").append(trimmedStep);

                // Thêm \\n để lưu database (trừ bước cuối)
                if (stepNumber < countNonEmptySteps(steps)) {
                    formattedSteps.append("\\n");
                }
                stepNumber++;
            }
        }

        return formattedSteps.toString();
    }

    private int countNonEmptySteps(String[] steps) {
        int count = 0;
        for (String step : steps) {
            if (!step.trim().isEmpty()) {
                count++;
            }
        }
        return count;
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
