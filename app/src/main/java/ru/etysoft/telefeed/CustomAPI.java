package ru.etysoft.telefeed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.etysoft.telefeed.activities.main.MainActivity;
import ru.etysoft.telefeed.api.Telegram;

public class CustomAPI extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custop_api);


        Button button = findViewById(R.id.button);
        Button buttonReset = findViewById(R.id.buttonReset);

        EditText appIdEditText = findViewById(R.id.appIdEditText);
        EditText apiHashEditText = findViewById(R.id.appHashEditText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyCustomApi(appIdEditText.getText().toString(), apiHashEditText.getText().toString());
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCustomApi();
            }
        });


    }

    private void applyCustomApi(String appId, String apiHash)
    {
        System.out.println("saved!");
        CacheUtils.getInstance().setString("apiId", appId, getApplicationContext());
        CacheUtils.getInstance().setString("apiHash", apiHash, getApplicationContext());




        System.exit(0);

    }

    public void resetCustomApi()
    {

        System.out.println("cleared");
        CacheUtils.getInstance().removeString("apiId",  getApplicationContext());
        CacheUtils.getInstance().removeString("apiHash",  getApplicationContext());


        System.exit(0);

    }
}