package ru.etysoft.telefeed.activities.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.api.Telegram;

public class WelcomeActivity extends AppCompatActivity implements Telegram.AuthorizationStateCallback {


    public static boolean shown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Telegram.addAuthorizationStateCallback(this);

        shown = true;
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Telegram.getClient().send(new TdApi.LogOut(), new Telegram.AuthorizationRequestHandler());
    }

    public void onNextButtonClick(View v)
    {
        TextView numberEditText = findViewById(R.id.numberEditText);
        String phoneNumber = numberEditText.getText().toString();

        TdApi.PhoneNumberAuthenticationSettings settings =
                new TdApi.PhoneNumberAuthenticationSettings();

        settings.allowFlashCall = false;
        settings.allowMissedCall = false;
        settings.allowSmsRetrieverApi = false;



        Telegram.getClient().send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, settings),
                new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(WelcomeActivity.this,
                                                getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });


                                break;
                        }
                    }
                });

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
            }
        });
    }

    @Override
    public void onWaitRegistration() {

    }

    @Override
    public void onAuthorizationReady() {
        finish();
    }
}