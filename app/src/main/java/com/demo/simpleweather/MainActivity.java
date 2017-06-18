package com.demo.simpleweather;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.demo.simpleweather.utils.L;


public class MainActivity extends AppCompatActivity {
    private CityManager cityManager;
    private ViewPager vpContainer;
    private int pagePosition = 0;

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

        vpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
                L.d(WeatherApplication.TAG, "position : " + position);
                cityManager.getCityFragment(position).suggestRefreshWeather();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        location();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cityManager.saveCityNames();
    }

    public int getCurrentPagePosition() {
        return pagePosition;
    }

    public void setPagePosition(int position) {
        vpContainer.setCurrentItem(position, true);
    }

    public CityManager getCityManager() {
        return cityManager;
    }

    //*****************************private******************************

    private void location() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setNeedAddress(true);
        option.setOnceLocationLatest(true);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);

        AMapLocationClient client = new AMapLocationClient(this);
        client.setLocationOption(option);
        client.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                L.d(WeatherApplication.TAG, "Location : " + aMapLocation.getCity());
                cityManager.addLocationCity(aMapLocation.getCity());
                cityManager.getCityFragment(0).suggestRefreshWeather();
            }
        });

        client.startLocation();
    }
}
