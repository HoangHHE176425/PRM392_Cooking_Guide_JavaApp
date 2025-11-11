package com.example.prm392_cooking_guide_javaapp.entity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_cooking_guide_javaapp.PostDetailActivity;
import com.example.prm392_cooking_guide_javaapp.R;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> posts;
    private final Context context;
    private final int currentUserId;

    private static final String TAG = "PostAdapterDebug";

    public PostAdapter(List<Post> posts, Context context, int currentUserId) {
        this.posts = posts;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, contentTextView, timeTextView;
        ImageView postImageView, videoPlayIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            videoPlayIcon = itemView.findViewById(R.id.videoPlayIcon);

            // ✅ Bấm mở PostDetailActivity
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Post clickedPost = posts.get(pos);
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("POST_ID", clickedPost.getId());
                    intent.putExtra("USER_ID", currentUserId);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Post post) {
            Log.d(TAG, "🟢 Binding post ID=" + post.getId());

            usernameTextView.setText(
                    (post.getUsername() != null && !post.getUsername().isEmpty())
                            ? post.getUsername()
                            : "User ID: " + post.getUserId()
            );

            contentTextView.setText(post.getContent());
            timeTextView.setText(post.getCreatedAt() != null ? post.getCreatedAt() : "");

            String mediaUrl = post.getMediaUrl();
            String mediaType = post.getMediaType();

            // 🔹 Không có media
            if (mediaUrl == null || mediaUrl.isEmpty()) {
                postImageView.setVisibility(View.GONE);
                if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.GONE);
                return;
            }

            try {
                Uri uri = Uri.parse(mediaUrl);
                Bitmap bitmap = null;

                // ================================
                // 🖼️ Nếu là hình ảnh
                // ================================
                if ("image".equalsIgnoreCase(mediaType)) {
                    Log.d(TAG, "📸 Loading image: " + mediaUrl);

                    if (mediaUrl.startsWith("content://")) {
                        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                            if (inputStream != null) bitmap = BitmapFactory.decodeStream(inputStream);
                        }
                    } else if (mediaUrl.startsWith("/")) {
                        File file = new File(mediaUrl);
                        if (file.exists()) bitmap = BitmapFactory.decodeFile(mediaUrl);
                        else Log.e(TAG, "❌ Image file not found: " + mediaUrl);
                    }

                    if (bitmap != null) {
                        postImageView.setVisibility(View.VISIBLE);
                        postImageView.setImageBitmap(bitmap);
                        if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.GONE);
                        Log.d(TAG, "✅ Image loaded successfully.");
                    } else {
                        postImageView.setVisibility(View.GONE);
                        if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.GONE);
                        Log.e(TAG, "❌ Failed to decode image.");
                    }
                }

                // ================================
                // 🎥 Nếu là video
                // ================================
                else if ("video".equalsIgnoreCase(mediaType)) {
                    Log.d(TAG, "🎬 Loading video thumbnail: " + mediaUrl);

                    String realPath = getRealPathFromURI(context, uri);
                    if (realPath != null) {
                        bitmap = ThumbnailUtils.createVideoThumbnail(realPath, MediaStore.Video.Thumbnails.MINI_KIND);
                    } else {
                        Log.e(TAG, "❌ getRealPathFromURI() returned null");
                    }

                    if (bitmap != null) {
                        postImageView.setVisibility(View.VISIBLE);
                        postImageView.setImageBitmap(bitmap);
                        if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.VISIBLE);
                        Log.d(TAG, "✅ Video thumbnail loaded.");
                    } else {
                        postImageView.setVisibility(View.GONE);
                        if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.GONE);
                        Log.e(TAG, "❌ Failed to generate video thumbnail.");
                    }
                }

                // ================================
                // ❌ Media không hợp lệ
                // ================================
                else {
                    postImageView.setVisibility(View.GONE);
                    if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.GONE);
                    Log.w(TAG, "⚠️ Unknown mediaType: " + mediaType);
                }

            } catch (Exception e) {
                Log.e(TAG, "🔥 Exception in bind(): " + e.getMessage(), e);
                postImageView.setVisibility(View.GONE);
                if (videoPlayIcon != null) videoPlayIcon.setVisibility(View.GONE);
            }
        }
    }

    // ==================================================
    // 🔧 Hàm hỗ trợ: Lấy đường dẫn thực từ URI content://
    // ==================================================
    private String getRealPathFromURI(Context context, Uri uri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        try (android.database.Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null)) {
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ getRealPathFromURI() error: " + e.getMessage());
        }
        return null;
    }
}
