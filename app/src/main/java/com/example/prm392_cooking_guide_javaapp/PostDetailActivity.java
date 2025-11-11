package com.example.prm392_cooking_guide_javaapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_cooking_guide_javaapp.dao.CommentDAO;
import com.example.prm392_cooking_guide_javaapp.dao.PostDAO;
import com.example.prm392_cooking_guide_javaapp.entity.Comment;
import com.example.prm392_cooking_guide_javaapp.entity.CommentAdapter;
import com.example.prm392_cooking_guide_javaapp.entity.Post;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView usernameTextView, timeTextView, contentTextView;
    private ImageView postMediaView, sendCommentButton;
    private EditText commentEditText;
    private RecyclerView commentsRecyclerView;

    private int postId, currentUserId;
    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initViews();

        postId = getIntent().getIntExtra("POST_ID", 0);
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        loadPostDetail();
        loadComments();

        sendCommentButton.setOnClickListener(v -> addComment());
    }

    private void initViews() {
        usernameTextView = findViewById(R.id.usernameTextView);
        timeTextView = findViewById(R.id.timeTextView);
        contentTextView = findViewById(R.id.contentTextView);
        postMediaView = findViewById(R.id.postMediaView);
        commentEditText = findViewById(R.id.commentEditText);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void loadPostDetail() {
        PostDAO.getPostById(postId, new PostDAO.PostDetailCallback() {
            @Override
            public void onSuccess(Post post) {
                usernameTextView.setText(post.getUsername());
                contentTextView.setText(post.getContent());
                timeTextView.setText(post.getCreatedAt());

                if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(post.getMediaUrl());
                    if (bitmap != null) {
                        postMediaView.setVisibility(ImageView.VISIBLE);
                        postMediaView.setImageBitmap(bitmap);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PostDetailActivity.this, "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        CommentDAO.getCommentsByPostId(postId, new CommentDAO.CommentCallback() {
            @Override
            public void onSuccess(List<Comment> comments) {
                commentList.clear();
                commentList.addAll(comments);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PostDetailActivity.this, "Không thể tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment() {
        String text = commentEditText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(currentUserId);
        comment.setContent(text);

        CommentDAO.addComment(comment, new CommentDAO.AddCommentCallback() {
            @Override
            public void onSuccess() {
                commentEditText.setText("");
                Toast.makeText(PostDetailActivity.this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PostDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
