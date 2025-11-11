package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.dao.PostDAO;
import com.example.myapplication.entity.Post;
import com.example.myapplication.entity.PostAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private RecyclerView postRecyclerView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton addPostFab;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        initViews();
        setupBottomNavigation();
        loadPosts();

        // 👉 Thêm FAB listener ở đây
        addPostFab.setOnClickListener(v -> {
            Intent intent = new Intent(ExploreActivity.this, AddPostActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });
    }

    private void initViews() {
        postRecyclerView = findViewById(R.id.postRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        addPostFab = findViewById(R.id.addPostFab); // 👈 thêm dòng này
        currentUserId = getIntent().getIntExtra("USER_ID", 0);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_explore);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_explore) {
                return true;
            } else if (id == R.id.nav_favorites) {
                Intent intent = new Intent(this, FavoritesActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadPosts() {
        PostDAO.getAllPosts(new PostDAO.PostCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                postList.clear();
                postList.addAll(posts);
                postAdapter = new PostAdapter(postList, ExploreActivity.this, currentUserId);
                postRecyclerView.setLayoutManager(new LinearLayoutManager(ExploreActivity.this));
                postRecyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ExploreActivity.this, "Không thể tải bài đăng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }
}
