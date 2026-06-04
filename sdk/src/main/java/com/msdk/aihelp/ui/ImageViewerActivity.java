package com.msdk.aihelp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.msdk.aihelp.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_URL = "image_url";

    public static void start(Context context, String imageUrl) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aihelp_activity_image_viewer);

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        ImageView ivImage = findViewById(R.id.iv_full_image);
        ImageView btnClose = findViewById(R.id.btn_close);

        Glide.with(this).load(imageUrl).into(ivImage);
        btnClose.setOnClickListener(v -> finish());
        ivImage.setOnClickListener(v -> finish());
    }
}
