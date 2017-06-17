package com.demo.simpleweather;


import android.app.Application;
import android.content.Context;

import okhttp3.OkHttpClient;


public class WApplication extends Application {
    private static WApplication wApplication;
    private OkHttpClient okHttpClient;
    private int dpi;

    public WApplication() {
        wApplication = this;
        okHttpClient = new OkHttpClient();
    }

    public static WApplication getInstance() {
        return wApplication;
    }

    public static Context getContext() {
        return wApplication;
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
