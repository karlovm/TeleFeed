package ru.etysoft.telefeed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class SmartImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Bitmap bitmap;


    public SmartImageView(Context context) {
        super(context);
    }

    public SmartImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm == bitmap) return;
        bitmap = bm;

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1f);
        alphaAnimation.setDuration(200);

        startAnimation(alphaAnimation);
        super.setImageBitmap(bm);
    }
}
