package ru.etysoft.telefeed.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.api.NewsGetter;
import ru.etysoft.telefeed.api.Telegram;
import ru.etysoft.telefeed.activities.auth.WelcomeActivity;

public class MainActivity extends AppCompatActivity implements Telegram.AuthorizationStateCallback {


    private NewsAdapter newsAdapter;
    private int lastCount = 0;
    private List<NewsAdapter.Article> articles = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Telegram.initialize(getApplicationContext());
        Telegram.addAuthorizationStateCallback(this);






    }


    public void init()
    {
        try {
            newsAdapter = new NewsAdapter(this, articles);
            RecyclerView recyclerView = findViewById(R.id.recycler_view);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                    {

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


            Thread watchdog = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {


                        try {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView textView = findViewById(R.id.textView3);

                                    try {
                                        int count = NewsGetter.getArticles().size();
                                        textView.setText("art: " + count);
                                        if(count - lastCount > 100)
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    apply(null);
                                                }
                                            });

                                        }
                                        if(lastCount == count)
                                        {
                                            textView.setText("!art: " + count);
                                        }
                                        lastCount = count;
                                    } catch (CloneNotSupportedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            watchdog.start();

            recyclerView.setAdapter(newsAdapter);
            newsAdapter.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void apply(View v)
    {
        articles.clear();
        try {
            List<NewsAdapter.Article> articles2 = new ArrayList<>(NewsGetter.getArticles());
            Collections.reverse(articles2);
            List<Integer> dates = new ArrayList<>();

            for(NewsAdapter.Article article : articles2)
            {
                dates.add(article.getDate());
            }

            Collections.sort(dates);

            List<NewsAdapter.Article> chronicArticles = new ArrayList<>();

            for(int date : dates)
            {
                List<NewsAdapter.Article> articlesToDate = new ArrayList<>();
                for(NewsAdapter.Article article : articles2)
                {
                    if(article.getDate() == date)
                    {
                        articlesToDate.add(article);
                    }
                }
                chronicArticles.addAll(articlesToDate);
            }

            Collections.reverse(chronicArticles);
            articles.addAll(chronicArticles);
        } catch (Exception e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsAdapter.notifyDataSetChanged();
            }
        });
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWaitCode() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWaitRegistration() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(WelcomeActivity.shown) return;
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAuthorizationReady() {
        init();
    }
}