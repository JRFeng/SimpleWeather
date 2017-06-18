package com.demo.simpleweather;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.demo.simpleweather.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class CityManager {
    private LinkedList<String> mCityNames;
    private LinkedList<CityFragment> mCityFragments;
    private FragmentStatePagerAdapter mPagerAdapter;

    private final String cityFileName = "city_names.dat";

    public CityManager(FragmentManager fragmentManager) {
        createPagerAdapter(fragmentManager);
    }

    //******************************public**********************************

    /**
     * 保存城市列表到本地
     */
    public void saveCityNames() {
        new Thread() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = WeatherApplication.getContext().openFileOutput(cityFileName, Context.MODE_PRIVATE);
                    ObjectOutputStream output = new ObjectOutputStream(outputStream);
                    output.writeObject(mCityNames);
                    output.close();
                } catch (IOException e) {
                    L.e(WeatherApplication.TAG, "Save city list error!!! maybe something is wrong!!!");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 返回一个ViewPager适配器
     * @return FragmentStatePagerAdapter
     */
    public FragmentStatePagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    /**
     * 在城市列表的末尾添加指定城市
     * @param cityName 城市名称
     */
    public void addCity(String cityName) {
        mCityNames.add(cityName);

        CityFragment cityFragment = new CityFragment();
        Bundle args = new Bundle();
        args.putString(CityFragment.KEY_CITY_NAME, cityName);
        args.putBoolean(CityFragment.KEY_IS_HOME, false);
        args.putInt(CityFragment.KEY_POSITION, mCityNames.size() - 1);
        cityFragment.setArguments(args);
        mCityFragments.add(cityFragment);

        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 在指定 index 位置添加城市
     * @param index 要添加到的位置
     * @param cityName 城市名称
     */
    public void addCity(int index, String cityName) {
        mCityNames.add(index, cityName);

        CityFragment cityFragment = new CityFragment();
        Bundle args = new Bundle();
        args.putString(CityFragment.KEY_CITY_NAME, cityName);
        args.putBoolean(CityFragment.KEY_IS_HOME, false);
        args.putInt(CityFragment.KEY_POSITION, mCityNames.size() - 1);
        cityFragment.setArguments(args);
        mCityFragments.add(index, cityFragment);

        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 删除指定 index 处的页面
     * @param index 要删除页面的位置
     */
    public void deleteCity(int index) {
        mCityNames.remove(index);
        mCityFragments.remove(index);
        for (int i = index; i < mCityFragments.size(); i++) {
            mCityFragments.get(index).getArguments().putInt(CityFragment.KEY_POSITION, index);
        }
        mPagerAdapter.notifyDataSetChanged();
    }

    //*******************************private********************************************

    //从本地恢复城市列表
    private void restoreCityNames() {
        try {
            InputStream inputStream = WeatherApplication.getContext().openFileInput(cityFileName);
            ObjectInputStream input = new ObjectInputStream(inputStream);
            mCityNames = (LinkedList<String>) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            L.d(WeatherApplication.TAG, "Restore city fail, create a empty city list");
            mCityNames = new LinkedList<>();
            mCityNames.add("N/A");
        }
    }

    private void createPagerAdapter(FragmentManager fragmentManager) {
        restoreCityNames();
        mCityFragments = new LinkedList<>();
        for (int i = 0; i < mCityNames.size(); i++) {
            CityFragment cityFragment = new CityFragment();
            Bundle args = new Bundle();
            args.putString(CityFragment.KEY_CITY_NAME, mCityNames.get(i));
            args.putBoolean(CityFragment.KEY_IS_HOME, i == 0);
            args.putInt(CityFragment.KEY_POSITION, i);
            cityFragment.setArguments(args);

            mCityFragments.add(cityFragment);
        }

        mPagerAdapter = new CityPagerAdapter(fragmentManager, mCityFragments);
    }
}
