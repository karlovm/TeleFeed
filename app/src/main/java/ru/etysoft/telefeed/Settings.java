package ru.etysoft.telefeed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ru.etysoft.telefeed.activities.auth.WelcomeActivity;
import ru.etysoft.telefeed.activities.main.MainActivity;
import ru.etysoft.telefeed.api.Telegram;
import ru.etysoft.telefeed.bottomsheet.IgnoreBottomSheet;
import ru.etysoft.telefeed.bottomsheet.ShareBottomSheet;

public class Settings extends AppCompatActivity implements IgnoreBottomSheet.BottomSheetListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        Switch switchDynamic = findViewById(R.id.switch_dynamic);

        if(CacheUtils.getInstance().getBoolean("dynamicArt", getApplicationContext()))
        {
            switchDynamic.setChecked(true);
        }

        switchDynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchDynamic.isChecked())
                {
                    CacheUtils.getInstance().setBoolean("dynamicArt", true,  getApplicationContext());

                    Toast toast = Toast.makeText( getApplicationContext(), R.string.dynamic_on, Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    CacheUtils.getInstance().setBoolean("dynamicArt", false,  getApplicationContext());
                }
            }
        });

        updateCacheSize();

    }

    public void updateCacheSize()
    {
        long size = folderSize(new File((getExternalFilesDir(null).getAbsolutePath() + "/")));

        double mb = size / 1024D / 1024D;
        TextView sizeTextView = findViewById(R.id.size);
        sizeTextView.setText(getResources().getString(R.string.settings_cache_size).replace("%s",String.valueOf(round(mb, 2))));
    }

    void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public void openApiActivity(View v)
    {
        Intent intent = new Intent(Settings.this, CustomAPI.class);
        startActivity(intent);
    }

    public void logout(View v)
    {
        Intent intent = new Intent(Settings.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }


     public void clearCache(View v)
    {
      // deleteDir(new File((getExternalFilesDir(null).getAbsolutePath() + "/")));
       updateCacheSize();


    }

    public void back(View v)
    {
        onBackPressed();
    }

    public void openIgnore(View v)
    {
        IgnoreBottomSheet bottomSheet = new IgnoreBottomSheet();

        bottomSheet.show(((FragmentActivity) this).getSupportFragmentManager(), "ignore");
    }
}