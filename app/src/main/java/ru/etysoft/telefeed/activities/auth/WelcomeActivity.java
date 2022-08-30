package ru.etysoft.telefeed.activities.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.etysoft.telefeed.CustomAPI;
import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.activities.main.MainActivity;
import ru.etysoft.telefeed.api.Telegram;

public class WelcomeActivity extends AppCompatActivity implements Telegram.AuthorizationStateCallback {


    public static boolean shown = false;
    public static boolean processing = false;

    private boolean isWaitingPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Telegram.addAuthorizationStateCallback(this);


        Telegram.getClient().send(new TdApi.LogOut(), new Telegram.AuthorizationRequestHandler());

        findViewById(R.id.helpView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, CustomAPI.class);
                startActivity(intent);
            }
        });

        shown = true;
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    public void onNextButtonClick(View v)
    {
        if(processing) return;
        processing = true;
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        v.setEnabled(false);
        TextView numberEditText = findViewById(R.id.numberEditText);
        String phoneNumber = numberEditText.getText().toString();

        TdApi.PhoneNumberAuthenticationSettings settings =
                new TdApi.PhoneNumberAuthenticationSettings();

        settings.allowFlashCall = false;
        settings.allowMissedCall = false;
        settings.allowSmsRetrieverApi = false;


        if(isWaitingPassword) {
            String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
            Telegram.getClient().send(new TdApi.CheckAuthenticationPassword(password), new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.Object object) {
                    processing = false;
                    switch (object.getConstructor()) {
                        case TdApi.Error.CONSTRUCTOR:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(WelcomeActivity.this,
                                            getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                                    toast.show();
                                    findViewById(R.id.helpView).setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    v.setEnabled(true);
                                }
                            });


                            break;
                    }
                }
            });
        }
        else {
            Telegram.getClient().send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, settings),
                    new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {
                            processing = false;
                            switch (object.getConstructor()) {
                                case TdApi.Error.CONSTRUCTOR:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(WelcomeActivity.this,
                                                    getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                                            toast.show();
                                            findViewById(R.id.helpView).setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                            v.setEnabled(true);
                                        }
                                    });


                                    break;
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shown = false;
        Telegram.removeAuthorizationStateCallback(this);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onWaitPhoneNumber() {

    }

    @Override
    public void onWaitCode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, CodeActivity.class);
                startActivity(intent);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                findViewById(R.id.button).setEnabled(true);
                findViewById(R.id.passwordEditText).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onWaitRegistration() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.numberEditText).setVisibility(View.VISIBLE);
                findViewById(R.id.passwordEditText).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onWaitPassword() {
        isWaitingPassword = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.numberEditText).setVisibility(View.GONE);
                findViewById(R.id.passwordEditText).setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onAuthorizationReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.isAuth = true;
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}