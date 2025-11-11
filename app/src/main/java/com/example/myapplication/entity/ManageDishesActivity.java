package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.entity.AdminDishAdapter; // SỬA: từ entity thành adapter
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.util.ArrayList;
import java.util.List;

public class ManageDishesActivity extends AppCompatActivity {
    private RecyclerView dishesRecyclerView;
    private AdminDishAdapter dishAdapter;
    private List<Dish> dishesList;
    private ImageView backButton;
    private static final int EDIT_DISH_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_dishes);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadAllDishes(); // SỬA: từ loadDishData thành loadAllDishes
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        dishesRecyclerView = findViewById(R.id.dishesRecyclerView);
        dishesList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        dishAdapter = new AdminDishAdapter(dishesList, this::onEditDish, this::onDeleteDish);
        dishesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dishesRecyclerView.setAdapter(dishAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    // SỬA: Method này phải là loadAllDishes, không phải loadDishData
    private void loadAllDishes() {
        DishDAO.getAllDishes(new DishDAO.DishCallback() { // Thử dùng DishCallback
            @Override
            public void onSuccess(List<Dish> dishes) {
                dishesList.clear();
                dishesList.addAll(dishes);
                dishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ManageDishesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void onEditDish(Dish dish) {
        Intent intent = new Intent(this, EditDishActivity.class);
        intent.putExtra("DISH_ID", dish.getId());
        startActivityForResult(intent, EDIT_DISH_REQUEST);
    }

    private void onDeleteDish(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có chắc muốn xóa món ăn " + dish.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    DishDAO.deleteDish(dish.getId(), new DishDAO.DeleteCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ManageDishesActivity.this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                            loadAllDishes();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(ManageDishesActivity.this, "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_DISH_REQUEST && resultCode == RESULT_OK) {
            loadAllDishes();
            Toast.makeText(this, "Đã cập nhật món ăn", Toast.LENGTH_SHORT).show();
        }
    }
}
