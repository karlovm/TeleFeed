package ru.etysoft.telefeed.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.SliderActivity;
import ru.etysoft.telefeed.api.Telegram;


public class CommentsActivity extends AppCompatActivity {


    public static TdApi.Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);
        ProgressBar progressBar = findViewById(R.id.progress_circular);
        LinearLayout error = findViewById(R.id.error);
        Thread getInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                if (message.canGetMessageThread) {
                    TdApi.GetMessageThreadHistory getComments = new TdApi.GetMessageThreadHistory();
                    getComments.chatId = message.chatId;
                    getComments.limit = 100;
                    getComments.fromMessageId = 0;
                    getComments.messageId = message.id;

                    TdApi.GetMessageThread getMessageThread = new TdApi.GetMessageThread();
                    getMessageThread.messageId = message.id;
                    getMessageThread.chatId = message.chatId;


                    Telegram.getClient().send(getMessageThread, new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {

                            if (object instanceof TdApi.MessageThreadInfo) {
                                Telegram.getClient().send(getComments, new Client.ResultHandler() {
                                    @Override
                                    public void onResult(TdApi.Object object) {

                                        ArrayList<CommentsAdapter.Comment> comments = new ArrayList<>();
                                        if (object instanceof TdApi.Messages) {
                                            for (TdApi.Message message : ((TdApi.Messages) object).messages) {
                                                if(message.content instanceof TdApi.MessageText)
                                                {

                                                    CommentsAdapter.Comment comment =
                                                            new CommentsAdapter.Comment(((TdApi.MessageText) message.content).text.text);

                                                    comments.add(comment);
                                                }
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    RecyclerView recyclerView = findViewById(R.id.comments);
                                                    CommentsAdapter commentsAdapter = new CommentsAdapter(CommentsActivity.this, comments);
                                                    recyclerView.setAdapter(commentsAdapter);
                                                    commentsAdapter.notifyDataSetChanged();
                                                    progressBar.setVisibility(View.GONE);
                                                    error.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressBar.setVisibility(View.GONE);
                                                    error.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        error.setVisibility(View.VISIBLE);
                                    }
                                });

                            }
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            error.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        getInfo.start();
    }

    public void back(View v)
    {
        onBackPressed();
    }
}