package ru.etysoft.telefeed.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.etysoft.telefeed.CacheUtils;
import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.activities.main.ImagePreview;
import ru.etysoft.telefeed.api.MediaGetter;
import ru.etysoft.telefeed.api.MediaInfo;

public class ImagesPager  {

    private ViewPagerAdapter viewPagerAdapter;
    private List<MediaInfo> images = new ArrayList<>();
    private ImageCallback imageCallback;
    private ViewPager2 viewPager2;
    private Activity context;
    private boolean isClickable = true;
    private MediaPlayer mMediaPlayer;
    private List<DestroyListener> destroyListeners = new ArrayList<>();


    public ImagesPager(ViewPager2 viewPager2, Activity context)
    {
        this.viewPager2 = viewPager2;
        this.context = context;

        init();
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public List<MediaInfo> getImages() {
        return images;
    }


    public interface DestroyListener
    {
        void onDestroy();
    }

    public void onDestroy()
    {
        for(DestroyListener destroyListener : destroyListeners)
        {
            destroyListener.onDestroy();
        }
    }


    public void setImageCallback(ImageCallback imageCallback) {
        this.imageCallback = imageCallback;
    }

    public void init() {
        viewPagerAdapter = new ViewPagerAdapter(context, images);
        viewPager2.setAdapter(viewPagerAdapter);
    }

    public void setImages(List<MediaInfo> mediaInfos) {

        try {
            images = mediaInfos;
            viewPagerAdapter.setImages(mediaInfos);
            viewPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addImage(MediaInfo mediaInfo) {
        images.add(mediaInfo);
        viewPagerAdapter.notifyDataSetChanged();
    }

    public void addAllImage(List<MediaInfo> mediaInfos) {
        images.addAll(mediaInfos);
        viewPagerAdapter.notifyDataSetChanged();
    }

    public interface ImageCallback {
        void onImageClicked(int index);
    }

    public void focus()
    {
        images.get(viewPager2.getCurrentItem());

    }

    class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

        // Context object
        Context context;

        // Array of images
        List<MediaInfo> images;

        // Layout Inflater
        LayoutInflater mLayoutInflater;


        public void setImages(List<MediaInfo> images) {
            this.images = images;
        }

        // Viewpager Constructor
        public ViewPagerAdapter(Context context, List<MediaInfo> images) {
            this.context = context;
            this.images = images;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }




        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.image_item, parent, false);
            if(CacheUtils.getInstance().getBoolean("dynamicArt", context.getApplicationContext()))
            {
                view = mLayoutInflater.inflate(R.layout.image_item_dynamic, parent, false);
            }
            if(viewType == MediaInfo.TYPE_VIDEO)
            {
                view = mLayoutInflater.inflate(R.layout.video_item, parent, false);
            }

            return new ViewHolder(view);

        }

        public float convertPixelsToDp(float px, Context context){
            return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }

        public float convertDpToPixel(float dp, Context context){
            return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            MediaInfo mediaInfo = images.get(position);


            if(mediaInfo.getType() == MediaInfo.TYPE_IMAGE) {
                Bitmap bitmap = Bitmap.createBitmap(mediaInfo.getWidth(), mediaInfo.getHeight(), Bitmap.Config.ARGB_8888);
                holder.imageView.setImageBitmap(bitmap);
                holder.imageView.setVisibility(View.VISIBLE);

                if (!isClickable) {
                    holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }

                final Bitmap[] bitmap2 = {null};

                MediaGetter.getImage(mediaInfo.getId(), new MediaGetter.MediaResult() {
                    @Override
                    public int getType() {
                        return MediaInfo.TYPE_IMAGE;
                    }

                    @Override
                    public void onImageProcessed(Bitmap bitmap) {
                        bitmap2[0] = bitmap;
                        ImagesPager.this.context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (bitmap != null) {

                                }
                                holder.imageView.setImageBitmap(bitmap);
                                holder.imageView.setVisibility(View.VISIBLE);


                            }
                        });

                    }

                    @Override
                    public void onVideoProcessed(String path) {

                    }

                    @Override
                    public void onError() {
                        ImagesPager.this.context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.imageView.setImageResource(R.drawable.ic_error_placeholder);
                                holder.imageView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });


                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isClickable) return;
                        ImagePreview.position = position;
                        ImagePreview.images = images;
                        Intent intent = new Intent(context, ImagePreview.class);
                        context.startActivity(intent);
                    }
                });
            }
            else
            {

                destroyListeners.add(new DestroyListener() {
                    @Override
                    public void onDestroy() {
                        holder.videoView.destroy();
                    }
                });

                viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int pos) {
                        if (isClickable) return;
                        super.onPageSelected(position);
                        if(position == pos)
                        {
                            holder.videoView.focus();
                        }
                    }
                });

                holder.videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isClickable) return;
                        ImagePreview.position = position;
                        ImagePreview.images = images;
                        Intent intent = new Intent(context, ImagePreview.class);
                        context.startActivity(intent);
                    }
                });
                System.out.println("Video download started...");
                holder.videoView.setFeedVideoPlayerListener(new FeedVideoPlayer.FeedVideoPlayerListener() {
                    @Override
                    public void onFocused() {
                        AlphaAnimation animation1 = new AlphaAnimation(0f, 1f);
                        animation1.setDuration(100);
                        animation1.setFillAfter(true);
                        holder.muteStatusView.startAnimation(animation1);

                    }

                    @Override
                    public void onFocusExit() {
                        AlphaAnimation animation1 = new AlphaAnimation(1f, 0f);
                        animation1.setDuration(100);
                        animation1.setFillAfter(true);
                        holder.muteStatusView.startAnimation(animation1);
                    }
                });
                holder.muteStatusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.videoView.exitFocus();
                    }
                });
                MediaGetter.getImage(mediaInfo.getId(), new MediaGetter.MediaResult() {
                    @Override
                    public int getType() {
                        return MediaInfo.TYPE_VIDEO;
                    }

                    @Override
                    public void onImageProcessed(Bitmap bitmap) {



                    }

                    @Override
                    public void onVideoProcessed(String path) {



                            ImagesPager.this.context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float ratio = mediaInfo.getWidth() / (float) mediaInfo.getHeight();

                                    float height = holder.videoView.getWidth() / ratio;

                                    holder.videoView.getLayoutParams().height = (int) height;
                                    holder.videoView.requestLayout();

                                    holder.videoView.loadVideo(path);


                                }
                            });

                    }

                    @Override
                    public void onError() {
                        ImagesPager.this.context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(getType() == MediaInfo.TYPE_IMAGE) {
                                    holder.imageView.setImageResource(R.drawable.ic_error_placeholder);
                                    holder.imageView.setVisibility(View.VISIBLE);
                                }
                                else {
                                    System.out.println("Video error!");
                                }
                            }
                        });
                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        @Override
        public int getItemViewType(int position) {
           return images.get(position).getType();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {

            private ImageView imageView;
            private FeedVideoPlayer videoView;
            private ImageView muteStatusView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                videoView = itemView.findViewById(R.id.feedVideoPlayer);

                imageView = itemView.findViewById(R.id.imageViewMain);
                muteStatusView = itemView.findViewById(R.id.muteIcon);
            }
        }
    }
}
