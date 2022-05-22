package ru.etysoft.telefeed.activities.main;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;

import ru.etysoft.telefeed.R;

public class ImagePreview extends AppCompatActivity {

    public static Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ImageView imageView = findViewById(R.id.previewImage);
        imageView.setImageBitmap(image);
    }
}