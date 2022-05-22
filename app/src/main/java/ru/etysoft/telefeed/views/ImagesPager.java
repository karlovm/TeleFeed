package ru.etysoft.telefeed.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.activities.main.ImagePreview;
import ru.etysoft.telefeed.activities.main.NewsAdapter;

public class ImagesPager  {

    private ViewPagerAdapter viewPagerAdapter;
    private List<Bitmap> images = new ArrayList<>();
    private ImageCallback imageCallback;
    private ViewPager2 viewPager2;
    private Activity context;


    public ImagesPager(ViewPager2 viewPager2, Activity context)
    {
        this.viewPager2 = viewPager2;
        this.context = context;

        init();
    }

    public List<Bitmap> getImages() {
        return images;
    }




    public void setImageCallback(ImageCallback imageCallback) {
        this.imageCallback = imageCallback;
    }

    public void init() {
        viewPagerAdapter = new ViewPagerAdapter(context, images);
        viewPager2.setAdapter(viewPagerAdapter);
    }

    public void setImages(List<Bitmap> bitmapList) {

        try {
            images = bitmapList;
            viewPagerAdapter.setImages(bitmapList);
            viewPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addImage(Bitmap bitmap) {
        images.add(bitmap);
        viewPagerAdapter.notifyDataSetChanged();
    }

    public void addAllImage(List<Bitmap> bitmapList) {
        images.addAll(bitmapList);
        viewPagerAdapter.notifyDataSetChanged();
    }

    public interface ImageCallback {
        void onImageClicked(int index);
    }

    class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

        // Context object
        Context context;

        // Array of images
        List<Bitmap> images;

        // Layout Inflater
        LayoutInflater mLayoutInflater;


        public void setImages(List<Bitmap> images) {
            this.images = images;
        }

        // Viewpager Constructor
        public ViewPagerAdapter(Context context, List<Bitmap> images) {
            this.context = context;
            this.images = images;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }




        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.image_item, parent, false);
            return new ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Bitmap bitmap = images.get(position);
            if(bitmap != null) holder.imageView.setImageBitmap(bitmap);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImagePreview.image = bitmap;
                    Intent intent = new Intent(context, ImagePreview.class);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {

            private ImageView imageView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageViewMain);
            }
        }
    }
}
