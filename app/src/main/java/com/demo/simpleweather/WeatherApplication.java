package com.demo.simpleweather;


import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import okhttp3.OkHttpClient;


public class WeatherApplication extends Application {
    private static WeatherApplication weatherApplication;
    private OkHttpClient okHttpClient;
    private int dpi;

    private static int savedData;

    public static final String TAG = "SimpleWeather";

    @Override
    public void onCreate() {
        super.onCreate();
        weatherApplication = this;
        okHttpClient = new OkHttpClient();

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);

        dpi = displayMetrics.densityDpi;
    }

    public static WeatherApplication getInstance() {
        return weatherApplication;
    }

    public static Context getContext() {
        return weatherApplication;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public int getPx(int dp) {
        return dp * dpi / 160;
    }

    public static void setData(int data) {
        savedData = data;
    }

    public static int getData() {
        return savedData;
    }
}
