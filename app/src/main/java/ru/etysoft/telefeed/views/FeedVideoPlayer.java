package ru.etysoft.telefeed.views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import ru.etysoft.telefeed.activities.main.NewsAdapter;
import ru.etysoft.telefeed.api.NewsGetter;


public class FeedVideoPlayer extends TextureView {

    private static int globalId = 0;
    private static int focusedId = -1;


    private MediaPlayer mediaPlayer;
    private String videoPath;
    private boolean isReady = false;
    private boolean isFocused = false;
    private int id;
    private int recyclerViewPos;
    private FeedVideoPlayerListener feedVideoPlayerListener;

    public interface FeedVideoPlayerListener
    {
        void onFocused();
        void onFocusExit();
    }


    public void setFeedVideoPlayerListener(FeedVideoPlayerListener feedVideoPlayerListener) {
        this.feedVideoPlayerListener = feedVideoPlayerListener;
    }

    public FeedVideoPlayer(@NonNull Context context) {
        super(context);
        initialize();
    }

    public FeedVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FeedVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public FeedVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public void destroy()
    {
        reset();
    }

    public void loadVideo(String videoPath)
    {
        this.videoPath = videoPath;
        reset();
        isReady = false;

    }

    public boolean isReady() {
        return isReady;
    }

    public void setRecyclerViewPos(int recyclerViewPos) {
        this.recyclerViewPos = recyclerViewPos;
    }

    @Override
    public void setSurfaceTextureListener(@NonNull SurfaceTextureListener surfaceTexture) {

    }

    private void setSurfaceTexturePrivate(@NonNull SurfaceTextureListener surfaceTexture) {
        super.setSurfaceTextureListener(surfaceTexture);
    }

    public void reset()
    {
        if(mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = null;

    }

    public static void resetAllFocus()
    {
        focusedId = -1;
    }

    public void exitFocus()
    {
        focusedId = -1;
        if(feedVideoPlayerListener != null && !isFocused)
        {

            feedVideoPlayerListener.onFocusExit();
        }
        isFocused = false;
    }

    public void focus()
    {
        focusedId = id;
        if(feedVideoPlayerListener != null && !isFocused)
        {

            feedVideoPlayerListener.onFocused();
        }
        isFocused = true;
    }

    public void handleSurfaceAvailable(SurfaceTexture surfaceTexture)
    {
        Surface surface = new Surface(surfaceTexture);

        try {
            if(mediaPlayer == null)
            {
                mediaPlayer = new MediaPlayer();
            }
            if(!isReady && videoPath != null) {
                isReady = true;
                mediaPlayer.setDataSource(videoPath);
                mediaPlayer.setSurface(surface);



                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();

                      //  mediaPlayer.pause();
                    }
                });

                mediaPlayer.setLooping(true);

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        isReady = false;
                        return false;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    private void initialize()
    {
        mediaPlayer = new MediaPlayer();

        id = globalId;
        globalId++;

       /* setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                        motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER )
                {
                    focus();
                }
                return true;
            }
        });*/

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                {


                    if(focusedId != id)
                    {
                        if(mediaPlayer != null)
                        {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(mediaPlayer != null) {
                                        if(feedVideoPlayerListener != null && isFocused)
                                        {
                                            isFocused = false;
                                            feedVideoPlayerListener.onFocusExit();
                                        }
                                        mediaPlayer.setVolume(0.0F, 0.0F);
                                    }
                                }
                            });
                        }
                    }
                    else
                    {
                        if(mediaPlayer != null)
                        {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(mediaPlayer != null) {

                                        mediaPlayer.setVolume(1, 1);
                                    }
                                }
                            });
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            SurfaceTexture surfaceTexture = getSurfaceTexture();
                            if(surfaceTexture != null)
                            {

                                handleSurfaceAvailable(surfaceTexture);

                            }
                        }
                    });
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        thread.start();


    }


}
