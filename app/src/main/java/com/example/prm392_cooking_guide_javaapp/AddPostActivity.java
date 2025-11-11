package com.example.prm392_cooking_guide_javaapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm392_cooking_guide_javaapp.dao.PostDAO;
import com.example.prm392_cooking_guide_javaapp.entity.Post;
import java.io.*;

public class AddPostActivity extends AppCompatActivity {

    private EditText contentEditText;
    private ImageView mediaPreview;
    private VideoView videoPreview;
    private Button selectMediaButton, uploadButton;
    private Uri selectedUri;
    private String mediaType = "none";
    private String savedFilePath = null; // đường dẫn nội bộ file copy
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        contentEditText = findViewById(R.id.contentEditText);
        mediaPreview = findViewById(R.id.mediaPreview);
        videoPreview = findViewById(R.id.videoPreview);
        selectMediaButton = findViewById(R.id.selectMediaButton);
        uploadButton = findViewById(R.id.uploadButton);

        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        selectMediaButton.setOnClickListener(v -> openGallery());
        uploadButton.setOnClickListener(v -> uploadPost());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        pickMediaLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedUri = result.getData().getData();
                    if (selectedUri == null) return;

                    String mimeType = getContentResolver().getType(selectedUri);
                    if (mimeType == null) return;

                    try {
                        if (mimeType.startsWith("image/")) {
                            mediaType = "image";
                            mediaPreview.setVisibility(ImageView.VISIBLE);
                            videoPreview.setVisibility(VideoView.GONE);

                            // ✅ Sao chép file ảnh vào bộ nhớ riêng
                            savedFilePath = copyFileToInternalStorage(selectedUri, "dish_");

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(savedFilePath)));
                            mediaPreview.setImageBitmap(bitmap);

                        } else if (mimeType.startsWith("video/")) {
                            mediaType = "video";
                            mediaPreview.setVisibility(ImageView.GONE);
                            videoPreview.setVisibility(VideoView.VISIBLE);

                            // ✅ Sao chép video vào bộ nhớ riêng
                            savedFilePath = copyFileToInternalStorage(selectedUri, "video_");

                            videoPreview.setVideoPath(savedFilePath);
                            videoPreview.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Không thể tải file media!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // ============================================================
    // 📂 Hàm sao chép file từ URI vào bộ nhớ app
    // ============================================================
    private String copyFileToInternalStorage(Uri uri, String prefix) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("InputStream null for URI: " + uri);

        File outFile = new File(getFilesDir(), prefix + System.currentTimeMillis() + getFileExtension(uri));
        OutputStream outputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return outFile.getAbsolutePath(); // ✅ đường dẫn file thật
    }

    // Lấy đuôi file từ MIME type
    private String getFileExtension(Uri uri) {
        String type = getContentResolver().getType(uri);
        if (type == null) return ".dat";
        if (type.equals("image/jpeg")) return ".jpg";
        if (type.equals("image/png")) return ".png";
        if (type.equals("video/mp4")) return ".mp4";
        return ".dat";
    }

    private void uploadPost() {
        String content = contentEditText.getText().toString().trim();

        if (content.isEmpty() && savedFilePath == null) {
            Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn tệp!", Toast.LENGTH_SHORT).show();
            return;
        }

        Post newPost = new Post();
        newPost.setUserId(currentUserId);
        newPost.setContent(content);
        newPost.setMediaUrl(savedFilePath); // ✅ luôn là /data/user/0/... path thật
        newPost.setMediaType(mediaType);
        newPost.setVisibility("public");

        PostDAO.addPost(newPost, new PostDAO.AddPostCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddPostActivity.this, "✅ Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddPostActivity.this, "❌ Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
