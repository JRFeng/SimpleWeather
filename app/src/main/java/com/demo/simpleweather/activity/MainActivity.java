package com.demo.simpleweather.activity;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.simpleweather.adapter.viewpager.CityPagerAdapter;
import com.demo.simpleweather.data.City;
import com.demo.simpleweather.R;
import com.demo.simpleweather.SWApplication;
import com.demo.simpleweather.utils.L;


public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private CityPagerAdapter mPagerAdapter;
    private int mCurrentPosition;
    private ViewPager vpContainer;
    private TextView tvIndicator;


    //********************************Override*******************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L.d(SWApplication.TAG, "MainActivity : onCreate");

        L.d("App", "MainActivity : onCreate");

        mContext = this;

        vpContainer = (ViewPager) findViewById(R.id.vpContainer);
        mPagerAdapter = new CityPagerAdapter(getSupportFragmentManager());
        vpContainer.setAdapter(mPagerAdapter);

        tvIndicator = (TextView) findViewById(R.id.tvIndicator);

        //适配6.0的运行时权限
        if (checkPermission()) {
            location();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission_group.LOCATION}, 1);
            location();
        }

        addViewListener();
        updateIndicator();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPagerAdapter.save();
        L.d(SWApplication.TAG, "MainActivity : onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //调试
            L.d(SWApplication.TAG, "RESULT_OK : " + data.getStringExtra(AddCityActivity.KEY_RESULT));

            String cityName = data.getStringExtra(AddCityActivity.KEY_RESULT);
            City newCity = new City(cityName);
            if (!mPagerAdapter.addCity(newCity)) {
                if (mPagerAdapter.isFull()) {
                    Toast.makeText(mContext, "最多8个城市", Toast.LENGTH_SHORT).show();
                } else if (mPagerAdapter.contains(newCity)) {
                    Toast.makeText(mContext, "已存在", Toast.LENGTH_SHORT).show();
                    setCurrentPage(mPagerAdapter.getPosition(newCity), false);
                }
            } else {
                Toast.makeText(mContext, cityName + "：添加成功", Toast.LENGTH_SHORT).show();
                setCurrentPage(mPagerAdapter.getPosition(newCity), false);
            }
        } else {//调试else
            //调试
            L.d(SWApplication.TAG, "RESULT_CANCEL");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d(SWApplication.TAG, "-----------------");
        L.d(SWApplication.TAG, "City Names:");
        for (String i : mPagerAdapter.getCityNames()) {
            L.d(SWApplication.TAG, i);
        }
        L.d(SWApplication.TAG, "-----------------");
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
        return mPagerAdapter.getPosition(city);
    }

    public void deleteCity(final City city) {
        if (mPagerAdapter.getPosition(city) == 0) {
            Toast.makeText(mContext, "主页面无法删除", Toast.LENGTH_SHORT).show();
        } else {
            setCurrentPage(mPagerAdapter.getPosition(city) - 1, true);
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
                        mPagerAdapter.deleteCity(city);
                        updateIndicator();
                        vpContainer.removeOnPageChangeListener(this);
                    }
                }
            });
        }
    }

    public void undoDelete() {
        mPagerAdapter.undoDelete();
    }

    /**
     * 这个方法的主要的作用是动态修改指示器 tvIndicator 文
     * 本的颜色，这个功能依赖 CityFragment.suggestRefreshWeather() 方
     * 法和 CityFragment.refreshWeather() 方法。
     */
    public void setIndicatorColorRes(int colorId) {
        tvIndicator.setTextColor(getResources().getColor(colorId));
    }

    //*****************************private******************************

    private void location() {
        SWApplication.location(new SWApplication.OnLocationListener() {
            @Override
            public void onLocationChanged(City city) {
                mPagerAdapter.addCity(city);
            }
        });
    }

    private void addViewListener() {
        vpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mPagerAdapter.suggestRefreshWeather(position);

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
                        .setItems(mPagerAdapter.getCityNames(), new DialogInterface.OnClickListener() {
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
        int size = mPagerAdapter.getSize();
        if (size > 1) {
            tvIndicator.setVisibility(View.VISIBLE);
            tvIndicator.setText((mCurrentPosition + 1) + "/" + size);
        } else {
            tvIndicator.setVisibility(View.GONE);
        }
    }

    private boolean checkPermission() {
        int has = ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION);
        return has == PackageManager.PERMISSION_GRANTED;
    }
}
