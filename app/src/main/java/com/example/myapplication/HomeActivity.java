package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.Dish;
import com.example.myapplication.entity.FavoriteDishAdapter;
import com.example.myapplication.entity.RecentDish;
import com.example.myapplication.entity.RecentDishAdapter;
import com.example.myapplication.entity.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private ImageView profileImageView, notificationImageView;
    private EditText searchEditText;
    private RecyclerView favoriteDishRecyclerView, recentDishRecyclerView;
    private BottomNavigationView bottomNavigationView;

    private List<Dish> allFavoriteDishes = new ArrayList<>();
    private List<Dish> filteredFavoriteDishes = new ArrayList<>();

    private FavoriteDishAdapter favoriteDishAdapter;
    private RecentDishAdapter recentDishAdapter;
    private int currentUserId;

    // ✅ Dùng ActivityResultLauncher để nhận kết quả khi quay lại
    private final ActivityResultLauncher<Intent> allDishesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean favoritesUpdated = result.getData().getBooleanExtra("FAVORITES_UPDATED", false);
                    if (favoritesUpdated) {
                        // ✅ Reload lại danh sách yêu thích khi có thay đổi
                        loadMostFavoriteDishes();
                    }
                }
            });
    private final ActivityResultLauncher<Intent> dishDetailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean favoritesUpdated = result.getData().getBooleanExtra("FAVORITES_UPDATED", false);
                    if (favoritesUpdated) {
                        // ✅ Reload lại danh sách yêu thích
                        loadMostFavoriteDishes();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupUserInfo();
        loadMostFavoriteDishes();
        loadRecentDishes();
        setupBottomNavigation();
        setupViewAllClick();
        setupSearchFilter();
    }

    private void initViews() {
        userNameTextView = findViewById(R.id.userNameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        notificationImageView = findViewById(R.id.notificationImageView);
        searchEditText = findViewById(R.id.searchEditText);
        favoriteDishRecyclerView = findViewById(R.id.categoryRecyclerView);
        recentDishRecyclerView = findViewById(R.id.recentDishRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupUserInfo() {
        currentUserId = getIntent().getIntExtra("USER_ID", 0);
        String username = getIntent().getStringExtra("USERNAME");

        userNameTextView.setText("Tìm kiếm");

        loadUserAvatarFromDatabase();

        profileImageView.setOnClickListener(v -> openProfile());
        notificationImageView.setOnClickListener(v -> openNotifications());
    }

    private void loadUserAvatar(String avatarUrl, ImageView imageView) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("/")) {
                    Bitmap originalBitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (originalBitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(originalBitmap);
                        imageView.setImageBitmap(circularBitmap);
                    } else {
                        imageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));

        int x = (width - size) / 2;
        int y = (height - size) / 2;

        canvas.drawBitmap(bitmap, -x, -y, paint);

        return output;
    }

    private void loadUserAvatarFromDatabase() {
        UserDAO.getUserById(currentUserId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                loadUserAvatar(user.getAvatarUrl(), profileImageView);
            }

            @Override
            public void onError(String error) {
                // Silent error handling
            }
        });
    }

    private void loadMostFavoriteDishes() {
        DishDAO.getMostFavoriteDishes(6, new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                if (!dishes.isEmpty()) {
                    allFavoriteDishes.clear();
                    allFavoriteDishes.addAll(dishes);

                    filteredFavoriteDishes.clear();
                    filteredFavoriteDishes.addAll(dishes);

                    favoriteDishAdapter = new FavoriteDishAdapter(filteredFavoriteDishes, HomeActivity.this, currentUserId, HomeActivity.this::onFavoriteDishClick);
                    favoriteDishRecyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                    favoriteDishRecyclerView.setAdapter(favoriteDishAdapter);
                } else {
                    loadAllDishesAsFallback();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Không thể tải món yêu thích", Toast.LENGTH_SHORT).show();
                loadAllDishesAsFallback();
            }
        });
    }

    private void onFavoriteDishClick(Dish dish) {
        Intent intent = new Intent(this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", dish.getId());
        intent.putExtra("USER_ID", currentUserId);
        dishDetailLauncher.launch(intent); // ✅ thay startActivity
    }


    private void loadAllDishesAsFallback() {
        DishDAO.getAllDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                List<Dish> firstSix = dishes.subList(0, Math.min(6, dishes.size()));

                allFavoriteDishes.clear();
                allFavoriteDishes.addAll(firstSix);

                filteredFavoriteDishes.clear();
                filteredFavoriteDishes.addAll(firstSix);

                favoriteDishAdapter = new FavoriteDishAdapter(filteredFavoriteDishes, HomeActivity.this, currentUserId, HomeActivity.this::onFavoriteDishClick);
                favoriteDishRecyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                favoriteDishRecyclerView.setAdapter(favoriteDishAdapter);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecentDishes() {
        DishDAO.getRecentDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                List<RecentDish> recentDishes = new ArrayList<>();
                for (Dish dish : dishes) {
                    String timeAgo = calculateTimeAgo(dish.getCreatedAt());
                    recentDishes.add(new RecentDish(
                            dish.getName(),
                            timeAgo,
                            R.drawable.ic_dish_placeholder,
                            dish.getId(),
                            dish.getImageUrl()
                    ));
                }

                recentDishAdapter = new RecentDishAdapter(recentDishes, HomeActivity.this::onRecentDishClick);
                recentDishRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                recentDishRecyclerView.setAdapter(recentDishAdapter);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Không thể tải món gần đây", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchFilter() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void applySearchFilter(String query) {
        filteredFavoriteDishes.clear();

        if (query.trim().isEmpty()) {
            filteredFavoriteDishes.addAll(allFavoriteDishes);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Dish dish : allFavoriteDishes) {
                if (dish.getName().toLowerCase(Locale.getDefault()).contains(lowerQuery) ||
                        (dish.getDescription() != null && dish.getDescription().toLowerCase(Locale.getDefault()).contains(lowerQuery))) {
                    filteredFavoriteDishes.add(dish);
                }
            }
        }

        favoriteDishAdapter.notifyDataSetChanged();

        if (filteredFavoriteDishes.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy món phù hợp", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_explore) {
                Intent intent = new Intent(this, ExploreActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                openFavorites();
                return true;
            }

            return false;
        });
    }

    private void setupViewAllClick() {
        TextView allDishesTextView = findViewById(R.id.allDishesTextView);
        allDishesTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllDishesActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            allDishesLauncher.launch(intent); // ✅ Dùng launcher mới
        });
    }

    private void onRecentDishClick(RecentDish recentDish) {
        Intent intent = new Intent(HomeActivity.this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", recentDish.getDishId());
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    private void openNotifications() {
        Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
    }

    private void openFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserAvatarFromDatabase();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private String calculateTimeAgo(String createdAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date createdDate = sdf.parse(createdAt);
            Date currentDate = new Date();

            long diffInMillis = currentDate.getTime() - createdDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInDays == 0) return "Hôm nay";
            else if (diffInDays == 1) return "Hôm qua";
            else if (diffInDays < 7) return "Cách đây " + diffInDays + " ngày";
            else return "Cách đây " + (diffInDays / 7) + " tuần";
        } catch (Exception e) {
            return "Cách đây vài ngày";
        }
    }
}
