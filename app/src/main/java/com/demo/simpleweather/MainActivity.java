package com.demo.simpleweather;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.demo.simpleweather.utils.L;


public class MainActivity extends AppCompatActivity {
    private CityManager cityManager;
    private ViewPager vpContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        L.d("App", "onCreate");

        vpContainer = (ViewPager) findViewById(R.id.vpContainer);
        cityManager = new CityManager(getSupportFragmentManager());
        vpContainer.setAdapter(cityManager.getPagerAdapter());

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);

        WeatherApplication.getInstance().setDPI(displayMetrics.densityDpi);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cityManager.saveCityNames();
    }
}
