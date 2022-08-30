package ru.etysoft.telefeed.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.api.MediaInfo;
import ru.etysoft.telefeed.views.FeedVideoPlayer;
import ru.etysoft.telefeed.views.ImagesPager;

public class ImagePreview extends AppCompatActivity {

    public static List<MediaInfo> images;
    public static int position = 0;
    ImagesPager imagesPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ViewPager2 viewPager2 = findViewById(R.id.imagesPager);
        imagesPager = new ImagesPager(viewPager2, this);
        imagesPager.setImages(images);
        viewPager2.setOffscreenPageLimit(5);
        viewPager2.setCurrentItem(position);
        imagesPager.setClickable(false);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FeedVideoPlayer.resetAllFocus();
    }

    public void onExitPressed(View v)
    {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_out, R.anim.fade_out);
    }
}