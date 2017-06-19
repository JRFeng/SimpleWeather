package com.demo.simpleweather;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.Toast;

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

    private String tempCityName;
    private int tempCityPosition;
    private CityFragment tempCityFragment;

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
     *
     * @return FragmentStatePagerAdapter
     */
    public FragmentStatePagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    /**
     * 在城市列表的末尾添加指定城市
     *
     * @param cityName 城市名称
     */
    public boolean addCity(String cityName) {
        if (!contains(cityName)) {
            if (mCityNames.get(0).equals("N/A")) {
                deleteCity(0);
            }
            mCityNames.add(cityName);

            CityFragment cityFragment = new CityFragment();
            Bundle args = new Bundle();
            args.putString(CityFragment.KEY_CITY_NAME, cityName);
            args.putBoolean(CityFragment.KEY_IS_HOME, false);
            args.putInt(CityFragment.KEY_POSITION, mCityNames.size() - 1);
            cityFragment.setArguments(args);
            mCityFragments.add(cityFragment);

            mPagerAdapter.notifyDataSetChanged();

            return true;
        }
        return false;
    }

    /**
     * 在指定 index 位置添加城市
     *
     * @param index    要添加到的位置
     * @param cityName 城市名称
     */
    public boolean addCity(int index, String cityName) {
        if (!contains(cityName) && (getCitiesCount() <= 8)) {
            mCityNames.add(index, cityName);

            CityFragment cityFragment = new CityFragment();
            Bundle args = new Bundle();
            args.putString(CityFragment.KEY_CITY_NAME, cityName);
            args.putBoolean(CityFragment.KEY_IS_HOME, index == 0);
            args.putInt(CityFragment.KEY_POSITION, index);
            cityFragment.setArguments(args);
            mCityFragments.add(index, cityFragment);

            for (int i = index + 1; i < mCityFragments.size(); i++) {
                mCityFragments.get(i).getArguments().putInt(CityFragment.KEY_POSITION, i);
                mCityFragments.get(i).getArguments().putBoolean(CityFragment.KEY_IS_HOME, i == 0);
            }

            mPagerAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    /**
     * 将定位到的城市添加进城市列表
     *
     * @param cityName 当前定位到的城市
     */
    public void addLocationCity(String cityName) {
        if (mCityNames.get(0).equals("N/A")) {
            deleteCity(0);
            addCity(0, cityName);
        }

        if (!cityName.equals(mCityNames.get(0))) {
            addCity(0, cityName);
        }
    }

    /**
     * 删除指定 index 处的页面
     *
     * @param index 要删除页面的位置
     */
    public void deleteCity(int index) {
        if (!mCityFragments.get(index).isHomePage() || mCityNames.get(index).equals("N/A")) {
            tempCityPosition = index;                           //缓存位置
            tempCityName = mCityNames.remove(index);            //缓存城市名
            tempCityFragment = mCityFragments.remove(index);    //缓存CityFragment

            L.d(WeatherApplication.TAG, "Delete : " + tempCityName);

            for (int i = index; i < mCityFragments.size(); i++) {
                mCityFragments.get(i).getArguments().putInt(CityFragment.KEY_POSITION, i);
                mCityFragments.get(i).getArguments().putBoolean(CityFragment.KEY_IS_HOME, i == 0);
            }
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 撤销删除
     */
    public boolean undoDelete() {
        if (tempCityName == null || tempCityName.equals("N/A") || tempCityName.equals("")) {
            L.d(WeatherApplication.TAG, "恢复失败");
            return false;
        }

        mCityNames.add(tempCityPosition, tempCityName);
        mCityFragments.add(tempCityPosition, tempCityFragment);

        for (int i = tempCityPosition + 1; i < mCityFragments.size(); i++) {
            mCityFragments.get(i).getArguments().putInt(CityFragment.KEY_POSITION, i);
            mCityFragments.get(i).getArguments().putBoolean(CityFragment.KEY_IS_HOME, i == 0);
        }

        mPagerAdapter.notifyDataSetChanged();

        tempCityName = null;
        return true;
    }

    public int getCitiesCount() {
        return mCityFragments.size();
    }

    public CityFragment getCityFragment(int position) {
        return mCityFragments.get(position);
    }


    public boolean isAlreadyHave(String cityName) {
        return contains(cityName);
    }

    public boolean isFull() {
        return mCityNames.size() == 8;
    }

    public int getCityPosition(String cityName) {
        for (int i = 0; i < mCityNames.size(); i++) {
            if (cityName.equals(mCityNames.get(i))) {
                return i;
            }
        }
        return -1;
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

//            //Test Start 发布时请注释掉
//            mCityNames.add("北京市");
//            mCityNames.add("上海市");
            //Test End
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

    private boolean contains(String cityName) {
        for (int i = 0; i < mCityNames.size(); i++) {
            if (cityName.equals(mCityNames.get(i))) {
                return true;
            }
        }
        return false;
    }
}
