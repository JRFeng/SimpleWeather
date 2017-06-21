package com.demo.simpleweather.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.demo.simpleweather.data.City;
import com.demo.simpleweather.data.manager.CityManager;
import com.demo.simpleweather.R;
import com.demo.simpleweather.SwApplication;
import com.demo.simpleweather.utils.L;


public class MainActivity extends AppCompatActivity implements CityManager.OnDataChangedListener {
    private Context mContext;
    private CityManager mCityManager;
    private int mCurrentPosition;
    private ViewPager vpContainer;
    private TextView tvIndicator;

    public static final int DATA_CHANGED = 0;
    public static final int WEATHER_UPDATED = 1;

    //********************************Override*******************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        L.d("App", "MainActivity : onCreate");

        mContext = this;

        vpContainer = (ViewPager) findViewById(R.id.vpContainer);
        mCityManager = CityManager.getInstance();
        vpContainer.setAdapter(mCityManager.getPagerAdapter(getSupportFragmentManager()));

        tvIndicator = (TextView) findViewById(R.id.tvIndicator);

        addViewListener();
        location();
        updateIndicator();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCityManager.save();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //调试
            L.d(SwApplication.TAG, "RESULT_OK : " + data.getStringExtra(AddCityActivity.KEY_RESULT));

            String cityName = data.getStringExtra(AddCityActivity.KEY_RESULT);
            City newCity = new City(cityName);
            if (!mCityManager.addCity(newCity)) {
                if (mCityManager.isFull()) {
                    Toast.makeText(mContext, "最多8个城市", Toast.LENGTH_SHORT).show();
                } else if (mCityManager.contains(newCity)) {
                    Toast.makeText(mContext, "已存在", Toast.LENGTH_SHORT).show();
                    setCurrentPage(mCityManager.getPosition(newCity), false);
                }
            } else {
                Toast.makeText(mContext, cityName + "：添加成功", Toast.LENGTH_SHORT).show();
                setCurrentPage(mCityManager.getPosition(newCity), false);
            }
        } else {//调试else
            //调试
            L.d(SwApplication.TAG, "RESULT_CANCEL");
        }
    }

    //*****************************public*************************

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPage(int position, boolean scroll) {
        mCurrentPosition = position;
        vpContainer.setCurrentItem(position, scroll);
    }

    public int getPosition(City city) {
        return mCityManager.getPosition(city);
    }

    public void deleteCity(final City city) {
        if (mCityManager.getPosition(city) == 0) {
            Toast.makeText(mContext, "主页面无法删除", Toast.LENGTH_SHORT).show();
        } else {
            setCurrentPage(mCityManager.getPosition(city) - 1, true);
            vpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        mCityManager.deleteCity(city);
                        updateIndicator();
                        vpContainer.removeOnPageChangeListener(this);
                    }
                }
            });
        }
    }

    public void undoDelete() {
        mCityManager.undoDelete();
    }

    /**
     * 这个方法的主要的作用是动态修改指示器 tvIndicator 文
     * 本的颜色，这个功能对 CityFragment.suggestRefreshWeather() 方
     * 法和 CityFragment.refreshWeather() 方法的依赖度很高，下次重构时请注意！
     */
    public void setIndicatorColorRes(int colorId) {
        tvIndicator.setTextColor(getResources().getColor(colorId));
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
                    City city = new City(aMapLocation.getCity(), true);
                    mCityManager.addCity(city);
                    //调试
                    L.d(SwApplication.TAG, "定位成功 : " + aMapLocation.getCity());
                } else {
                    Toast.makeText(mContext, "定位失败", Toast.LENGTH_SHORT).show();

                    //调试
                    L.d(SwApplication.TAG, "定位失败 : " + aMapLocation.getErrorCode() + " : " + aMapLocation.getCity());
                }
            }
        });

        //调试
        L.d(SwApplication.TAG, "开始定位");
        client.startLocation();
    }

    private void addViewListener() {
        vpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mCityManager.suggestRefreshWeather(position);

                updateIndicator();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //指示器监听器
        tvIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                        .setItems(mCityManager.getCityNames(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                setCurrentPage(i, true);
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });
    }

    private void updateIndicator() {
        int size = mCityManager.getSize();
        if (size > 1) {
            tvIndicator.setVisibility(View.VISIBLE);
            tvIndicator.setText((mCurrentPosition + 1) + "/" + size);
        } else {
            tvIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDataChanged() {
        updateIndicator();
    }
}
