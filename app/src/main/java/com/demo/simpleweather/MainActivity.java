package com.demo.simpleweather;


import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

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

        L.d("App", "MainActivity : onCreate");

        vpContainer = (ViewPager) findViewById(R.id.vpContainer);
        cityManager = new CityManager(getSupportFragmentManager());
        vpContainer.setAdapter(cityManager.getPagerAdapter());

        location();
        initViewListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cityManager.saveCityNames();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String newCity = data.getStringExtra(AddCityActivity.KEY_RESULT);
            if (cityManager.isAlreadyHave(newCity)) {
                setCurrentPageNoScroll(cityManager.getCityPosition(newCity));
                Toast.makeText(MainActivity.this, "已存在", Toast.LENGTH_SHORT).show();
            } else if (cityManager.isFull()) {
                Toast.makeText(MainActivity.this, "最多8个城市", Toast.LENGTH_SHORT).show();
            } else {
                cityManager.addCity(data.getStringExtra(AddCityActivity.KEY_RESULT));
                setCurrentPageNoScroll(cityManager.getCitiesCount() - 1);
            }

            L.d(WeatherApplication.TAG, "RESULT_OK : " + data.getStringExtra(AddCityActivity.KEY_RESULT));
        } else {
            L.d(WeatherApplication.TAG, "RESULT_CANCEL");
        }
    }

    public int getCurrentPagePosition() {
        return pagePosition;
    }

    public void setCurrentPage(int position) {
        vpContainer.setCurrentItem(position, true);
    }

    public void setCurrentPageNoScroll(int position) {
        vpContainer.setCurrentItem(position, false);
    }

    public CityManager getCityManager() {
        return cityManager;
    }

    public void deleteCity(final int position) {
        if (position == 0) {
            Toast.makeText(WeatherApplication.getContext(), "主页面无法删除", Toast.LENGTH_SHORT).show();
            return;
        }

        setCurrentPage(position - 1);
        vpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                L.d(WeatherApplication.TAG, "onPageScrolled, Position : " + position);

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    cityManager.deleteCity(position);
                    vpContainer.removeOnPageChangeListener(this);
                }
            }
        });
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
                if (aMapLocation.getErrorCode() == 0) {
                    cityManager.addLocationCity(aMapLocation.getCity());
                    cityManager.getCityFragment(0).suggestRefreshWeather();

                    L.d(WeatherApplication.TAG, "定位成功 : " + aMapLocation.getCity());
                }else{
                    L.d(WeatherApplication.TAG, "定位失败 : " + aMapLocation.getErrorCode() + " : " + aMapLocation.getCity());
                }
            }
        });

        client.startLocation();
        L.d(WeatherApplication.TAG, "开始定位");
    }

    private void initViewListener() {
        //ViewPager监听器
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
    }
}
