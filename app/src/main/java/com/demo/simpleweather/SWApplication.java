package com.demo.simpleweather;


import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.demo.simpleweather.data.City;
import com.demo.simpleweather.utils.L;

import okhttp3.OkHttpClient;


public class SWApplication extends Application {
    private static SWApplication swApplication;
    private static OkHttpClient mOkHttpClient;
    private static int dpi;

    private static int savedData;

    public static final String TAG = "SimpleWeather";

    @Override
    public void onCreate() {
        super.onCreate();
        swApplication = this;
        mOkHttpClient = new OkHttpClient();
    }

    public static Context getContext() {
        return swApplication;
    }

    public static OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public static int getPx(int dp) {
        if (dpi == 0) {
            DisplayMetrics displayMetrics = swApplication.getResources().getDisplayMetrics();
            dpi = displayMetrics.densityDpi;
        }
        return dp * dpi / 160;
    }

    public static void setSharedData(int data) {
        savedData = data;
    }

    public static int getSharedData() {
        return savedData;
    }

    public static void location(final OnLocationListener listener) {

        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setNeedAddress(true);
        option.setOnceLocationLatest(true);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);

        AMapLocationClient client = new AMapLocationClient(swApplication);
        client.setLocationOption(option);
        client.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {
                    City city = new City(aMapLocation.getCity(), true);
                    listener.onLocationChanged(city);
                    L.d(SWApplication.TAG, "定位成功 : " + aMapLocation.getCity());
                } else {
                    Toast.makeText(swApplication, "定位失败", Toast.LENGTH_SHORT).show();

                    //调试
                    L.d(SWApplication.TAG, "定位失败 : " + aMapLocation.getErrorCode() + " : " + aMapLocation.getCity());
                }
            }
        });

        //调试
        L.d(SWApplication.TAG, "开始定位");
        client.startLocation();
    }


    //*****************Listener*****************

    public interface OnLocationListener {
        void onLocationChanged(City city);
    }
}
