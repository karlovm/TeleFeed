package ru.etysoft.telefeed.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ru.etysoft.telefeed.CacheUtils;
import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.SearchActivity;
import ru.etysoft.telefeed.Settings;
import ru.etysoft.telefeed.api.NewsGetter;
import ru.etysoft.telefeed.api.Telegram;
import ru.etysoft.telefeed.activities.auth.WelcomeActivity;
import ru.etysoft.telefeed.bottomsheet.ShareBottomSheet;


public class MainActivity extends AppCompatActivity implements Telegram.AuthorizationStateCallback, ShareBottomSheet.BottomSheetListener {


    public static boolean isAuth = false;
    public static boolean isDebug = false;
    public  RecyclerView recyclerView;
    public static List<Long> ignoreChannels = new ArrayList<>();

    private NewsAdapter newsAdapter;
    private int lastCount = 0;
    private List<NewsAdapter.Article> articles = new ArrayList<>();
    VideoView videoView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Telegram.addAuthorizationStateCallback(this);
        Telegram.initialize(getApplicationContext());


    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            ignoreChannels.clear();
            JSONArray jsonArray = new JSONArray(CacheUtils.getInstance().getString("ignoreList", getApplicationContext()));

            for(int i = 0; i < jsonArray.length(); i++)
            {
                ignoreChannels.add(jsonArray.getLong(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        videoView = (VideoView) findViewById(R.id.videoview);


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        } else {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }


        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sticker));
        videoView.start();
        if(isAuth)
        {

            init();
        }


        EditText searchView = findViewById(R.id.searchView);
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    SearchActivity.query = searchView.getText().toString();
                    SearchActivity.articles = articles;

                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    public void init() {
        if(articles.size() != 0) return;
        try {
            recyclerView = findViewById(R.id.recycler_view);
            newsAdapter = new NewsAdapter(this, articles);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        try {
                            NewsGetter.processChannels();
                            Thread.sleep(30000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            thread.start();


            Thread updater = new Thread(new Runnable() {
                @Override
                public void run() {
                   /* ArticleDao articleDao = ArticlesDatabase.getInstance(getApplicationContext()).getTrackDao();


                    articles.addAll(articleDao.getAllArticles());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsAdapter.notifyDataSetChanged();
                        }
                    });*/

                    while (true) {


                        try {
                            int count = NewsGetter.getArticles().size();


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView textView = findViewById(R.id.textView3);

                                    try {

                                        textView.setText("art: " + count);



                                        if (lastCount == count) {
                                            textView.setText("!art: " + count);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }});


                            apply(null);

                            lastCount = count;


                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            updater.start();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(newsAdapter);
                    newsAdapter.notifyDataSetChanged();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void apply(View v) {


        try {
            List<NewsAdapter.Article> articles2 = new ArrayList<>(NewsGetter.getArticles());


            Collections.reverse(articles2);




            List<Integer> dates = new ArrayList<>();

            for (NewsAdapter.Article article : articles2) {
                dates.add(article.getDate());
            }

            Collections.sort(dates);

            List<NewsAdapter.Article> chronicArticles = new ArrayList<>();

            List<Long> processedIds = new ArrayList<>();
            for (int date : dates) {
                List<NewsAdapter.Article> articlesToDate = new ArrayList<>();
                for (NewsAdapter.Article article : articles2) {
                    if (article.getDate() == date) {
                        if(!processedIds.contains(article.getMessageId())) {



                            articlesToDate.add(article);
                            processedIds.add(article.getMessageId());
                        }
                    }
                }
                chronicArticles.addAll(articlesToDate);
            }

            Collections.reverse(chronicArticles);




            HashMap<Integer, NewsAdapter.Article> insertDictionary = new HashMap<>();
            HashMap<Integer, NewsAdapter.Article> updateDictionary = new HashMap<>();

            int index = 0;
            int updateOffset = 0;
            List<Long> processedInsertIds = new ArrayList<>();
            if(articles.size() != 0) {

                for (NewsAdapter.Article article : articles) {



                    for (NewsAdapter.Article newArticle : chronicArticles) {
                        if (newArticle.getDate() > article.getDate()) {
                            boolean hasArticle = false;
                            for (NewsAdapter.Article articleContains : articles) {
                                if (articleContains.getMessageId() == newArticle.getMessageId()) {
                                    hasArticle = true;
                                    break;
                                }
                            }

                            if(newArticle.getText().equals(article.getText()) && newArticle.getMediaList().size() == article.getMediaList().size())
                            {
                                hasArticle = true;
                            }

                            if (!hasArticle && !processedInsertIds.contains(newArticle.getMessageId())) {
                               // System.out.println("Inserted " + newArticle.getText() + " (" + newArticle.getDate() + " > " + article.getDate());
                                insertDictionary.put(index, newArticle);
                                processedInsertIds.add(newArticle.getMessageId());
                            }
                        }
                          if (newArticle.getMessageId() == article.getMessageId()) {
                            if (!newArticle.getText().equals(article.getText()) ||
                                    newArticle.getMediaList().size() != article.getMediaList().size()) {
                                if (newArticle.getMediaList().size() >= article.getMediaList().size()) {
                                   // System.out.println("Updated " + newArticle.getText() + " (" + newArticle.getDate() + " > " + article.getDate());
                                    updateDictionary.put(index, newArticle);
                                }
                            }
                        }
                    }
                    index++;
                }

                for (int indexUpdate : updateDictionary.keySet()) {
                    articles.set(indexUpdate, updateDictionary.get(indexUpdate));

                    int firstVisibleIndex = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    int lastVisibleIndex =((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newsAdapter.notifyItemChanged(indexUpdate);

                            }
                        });



                }


                List<Integer> sortedIndexes = new ArrayList<>(insertDictionary.keySet());

                Collections.sort(sortedIndexes);


                for (int indexInsert : sortedIndexes) {
                    articles.add(indexInsert + updateOffset, insertDictionary.get(indexInsert));

                    //    newsAdapter.notifyItemMoved(indexInsert,  indexInsert+ 1);



                    int finalUpdateOffset = updateOffset;
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newsAdapter.notifyItemInserted(indexInsert + finalUpdateOffset);

                            }
                        });
                    updateOffset++;

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(newsAdapter.getItemCount() > 0)
                        {
                            LinearLayout loading = findViewById(R.id.loadingView);
                            loading.setVisibility(View.GONE);
                            videoView.stopPlayback();
                        }

                    }
                });
            }
            else
            {

                articles.addAll(chronicArticles);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        newsAdapter.notifyDataSetChanged();
                        if(newsAdapter.getItemCount() > 0)
                        {
                            LinearLayout loading = findViewById(R.id.loadingView);
                            loading.setVisibility(View.GONE);
                            videoView.stopPlayback();
                        }

                    }
                });
            }
/*
            ArticleDao articleDao = ArticlesDatabase.getInstance(getApplicationContext()).getTrackDao();

            List<Long> cachedIds = new ArrayList<>();
            for(NewsAdapter.Article article : articleDao.getAllArticles())
            {
                cachedIds.add(article.getMessageId());
            }
            List<NewsAdapter.Article> newCachedArticles = new ArrayList<>();
            for(NewsAdapter.Article article : articles)
            {
                if(!cachedIds.contains(article.getMessageId()))
                {
                    newCachedArticles.add(article);
                }
            }
            NewsAdapter.Article[] dsf = new NewsAdapter.Article[newCachedArticles.size()];
            newCachedArticles.toArray(dsf);
            articleDao.insertAll(dsf);*/

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void openSettings(View v)
    {
        Intent intent = new Intent(MainActivity.this, Settings.class);
        startActivity(intent);
    }

    public void odd2(View v) {
        //String code = ((TextView)findViewById(R.id.editTextTextPersonName)).getText().toString();

        // client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    public void onWaitPhoneNumber() {
        isAuth = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWaitCode() {
        isAuth = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWaitRegistration() {
        isAuth = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWaitPassword() {
        isAuth = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAuthorizationReady() {
        isAuth = true;
        init();
    }
}