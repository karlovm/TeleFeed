package ru.etysoft.telefeed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.etysoft.telefeed.activities.main.NewsAdapter;
import ru.etysoft.telefeed.api.NewsGetter;

public class SearchActivity extends AppCompatActivity {

    public static List<NewsAdapter.Article> articles;
    public static String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        processSearch();
        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);
    }


    public void processSearch() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (query.startsWith("channel:")) {
                    String chatId = query.replace("channel:", "");

                    TdApi.Chat chat = null;
                    for (TdApi.Chat chat1 : NewsGetter.supergroups.values()) {
                        if (chat1.id == Long.parseLong(chatId)) {
                            chat = chat1;
                            break;
                        }
                    }


                    if (chat != null) {

                        List<NewsAdapter.Article> filteredArticles = new ArrayList<>();

                        for (NewsAdapter.Article article : articles) {
                            if (article.getMessage().chatId == chat.id) {
                                filteredArticles.add(article);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.progress_circular).setVisibility(View.GONE);
                                if (filteredArticles.size() == 0) {
                                    findViewById(R.id.error).setVisibility(View.VISIBLE);

                                }
                                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                                NewsAdapter newsAdapter = new NewsAdapter(SearchActivity.this, filteredArticles);
                                recyclerView.setAdapter(newsAdapter);

                                newsAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(0);
                            }
                        });

                    }

                } else if (query.length() > 4) {

                    List<NewsAdapter.Article> filteredArticles = new ArrayList<>();

                    for (NewsAdapter.Article article : articles) {
                        TdApi.Chat chat = NewsGetter.supergroups.get(article.getMessage().chatId);
                        if (article.getText().toLowerCase().contains(query.toLowerCase()) ||
                                query.toLowerCase().equals(chat.title.toLowerCase())) {
                            filteredArticles.add(article);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (filteredArticles.size() == 0) {
                                findViewById(R.id.error).setVisibility(View.VISIBLE);

                            }
                            findViewById(R.id.progress_circular).setVisibility(View.GONE);
                            RecyclerView recyclerView = findViewById(R.id.recyclerView);
                            NewsAdapter newsAdapter = new NewsAdapter(SearchActivity.this, filteredArticles);
                            recyclerView.setAdapter(newsAdapter);
                            newsAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(0);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.error).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        thread.start();

    }

    public void back(View v) {
        onBackPressed();
    }
}