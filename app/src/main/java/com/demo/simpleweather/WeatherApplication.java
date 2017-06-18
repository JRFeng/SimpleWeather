package com.demo.simpleweather;


import android.app.Application;
import android.content.Context;

import okhttp3.OkHttpClient;


public class WeatherApplication extends Application {
    private static WeatherApplication weatherApplication;
    private OkHttpClient okHttpClient;
    private int dpi;

    public static final String TAG = "SimpleWeather";

    @Override
    public void onCreate() {
        super.onCreate();
        weatherApplication = this;
        okHttpClient = new OkHttpClient();
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

    public void setDPI(int ppi) {
        this.dpi = ppi;
    }

    public int getDPI() {
        return dpi;
    }
}
