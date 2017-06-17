package com.demo.simpleweather;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


public class MainActivity extends AppCompatActivity {
    private CityManager cityManager;
    private ViewPager vpContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("App", "onCreate");

        vpContainer = (ViewPager) findViewById(R.id.vpContainer);
        cityManager = CityManager.getInstance();
        vpContainer.setAdapter(cityManager.getCityPagerAdapter(getSupportFragmentManager()));

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);

        WApplication.getInstance().setDPI(displayMetrics.densityDpi);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cityManager.saveData();
    }

    public void scrollToPrevious() {
        vpContainer.setCurrentItem(vpContainer.getCurrentItem() - 1, true);
    }
}
