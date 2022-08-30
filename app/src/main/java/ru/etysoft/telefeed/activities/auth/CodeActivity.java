package ru.etysoft.telefeed.activities.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import ru.etysoft.telefeed.activities.main.MainActivity;
import ru.etysoft.telefeed.R;
import ru.etysoft.telefeed.SliderActivity;
import ru.etysoft.telefeed.api.Telegram;

public class CodeActivity extends AppCompatActivity implements Telegram.AuthorizationStateCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        Telegram.addAuthorizationStateCallback(this);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);
        findViewById(R.id.backArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackArrowCLick(view);
            }
        });

        findViewById(R.id.buttonExplore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView codeView = findViewById(R.id.editTextCode);
                String code = codeView.getText().toString();
                Telegram.getClient().send(new TdApi.CheckAuthenticationCode(code), new Telegram.AuthorizationRequestHandler());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Telegram.removeAuthorizationStateCallback(this);
    }

    public void onBackArrowCLick(View v)
    {
        onBackPressed();
    }

    @Override
    public void onWaitPhoneNumber() {
        finish();
    }

    @Override
    public void onWaitCode() {

    }

    @Override
    public void onWaitRegistration() {
        finish();
    }

    @Override
    public void onWaitPassword() {
        finish();
    }

    @Override
    public void onAuthorizationReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(CodeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}