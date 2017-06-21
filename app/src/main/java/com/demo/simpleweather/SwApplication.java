package com.demo.simpleweather;


import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import okhttp3.OkHttpClient;


public class SwApplication extends Application {
    private static SwApplication swApplication;
    private OkHttpClient okHttpClient;
    private int dpi;

    private static int savedData;
    private static int shardColor;

    public static final String TAG = "SimpleWeather";

    @Override
    public void onCreate() {
        super.onCreate();
        swApplication = this;
        okHttpClient = new OkHttpClient();

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);

        dpi = displayMetrics.densityDpi;
    }

    public static SwApplication getInstance() {
        return swApplication;
    }

    public static Context getContext() {
        return swApplication;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public int getPx(int dp) {
        return dp * dpi / 160;
    }

    public static void setSharedData(int data) {
        savedData = data;
    }

    public static int getSharedData() {
        return savedData;
    }
}
