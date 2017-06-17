package com.demo.simpleweather;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.demo.simpleweather.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;


/*
* 添加、删除城市功能异常
*/
public class CityManager {
    private static CityManager mInstance;
    private List<String> mCityNames;
    private List<CityFragment> mCityFragments;
    private CityPagerAdapter mPagerAdapter;

    private static final String CityFileName = "city_names.dat";

    private CityManager() {
        initCityNames();
        initCityFragments();
    }

    //*********************************************************

    private void initCityNames() {
        //TODO,待优化。因为是在主线程中进行IO，所有有ANR的风险。
        try {
            InputStream inputStream = WApplication.getContext().openFileInput(CityFileName);
            ObjectInputStream input = new ObjectInputStream(inputStream);
            mCityNames = (LinkedList<String>) input.readObject();
        } catch (IOException e) {
            mCityNames = new LinkedList<>();
            L.e("SimpleWeather", "IOException, Restore Error");
            mCityNames.add("N/A");
            mCityNames.add("北京市");
            mCityNames.add("广州市");
            location();
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void location() {

        AMapLocationClientOption locationClientOption = new AMapLocationClientOption();
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        locationClientOption.setOnceLocation(true);
        locationClientOption.setOnceLocationLatest(true);
        locationClientOption.setNeedAddress(true);

        AMapLocationClient locationClient = new AMapLocationClient(WApplication.getContext());
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                String city = aMapLocation.getCity();
                L.d("App", "Locate : " + city);
                addLocationCity(city);
                if (mCityNames.contains("N/A")) {
                    deleteCity("N/A");
                }
            }
        });
        locationClient.setLocationOption(locationClientOption);
        locationClient.startLocation();
        L.d("App", "Start Location");
    }

    private void addLocationCity(String cityName) {
        if (!cityName.equals(mCityNames.get(0))) {
            mCityNames.add(0, cityName);

            Bundle data = new Bundle();
            data.putString("cityName", cityName);
            data.putBoolean("isHomePage", true);

            CityFragment fragment = new CityFragment();
            fragment.setArguments(data);

            mCityFragments.add(0, fragment);

            mCityFragments.get(1).getArguments().putBoolean("isHomePage", false);
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    private void initCityFragments() {
        L.d("App", "initCityFragments");

        mCityFragments = new LinkedList<>();
        for (int i = 0; i < mCityNames.size(); i++) {
            Bundle data = new Bundle();
            data.putString("cityName", mCityNames.get(i));
            data.putBoolean("isHomePage", i == 0);

            CityFragment fragment = new CityFragment();
            fragment.setArguments(data);

            mCityFragments.add(fragment);
        }
    }

    //**********************************************************

    public static synchronized CityManager getInstance() {
        L.d("App", "CityManager.getInstance");
        if (mInstance == null) {
            mInstance = new CityManager();
        }
        return mInstance;
    }

    public CityPagerAdapter getCityPagerAdapter(FragmentManager fm) {
        mPagerAdapter = new CityPagerAdapter(fm, mCityFragments);
        return mPagerAdapter;
    }

    public void addCity(String cityName) {
        if (!mCityNames.contains(cityName)) {
            mCityNames.add(cityName);

            Bundle data = new Bundle();
            data.putString("cityName", cityName);

            CityFragment fragment = new CityFragment();
            fragment.setArguments(data);

            mCityFragments.add(fragment);
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void deleteCity(String name) {
        if (mCityNames.size() > 1) {
            int position = mCityNames.indexOf(name);
            mCityNames.remove(name);
            mCityFragments.remove(position);
            mPagerAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(WApplication.getContext(), "无法删除", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveData() {
        //TODO,待优化。因为是在主线程中进行IO，所有有ANR的风险。
        try {
            OutputStream outputStream = WApplication.getContext().openFileOutput(CityFileName, Context.MODE_PRIVATE);
            ObjectOutputStream output = new ObjectOutputStream(outputStream);
            output.writeObject(mCityNames);
            output.close();
        } catch (IOException e) {
            L.e("SimpleWeather", "IOException, Save Error");
            e.printStackTrace();
        }
    }
}
