package com.demo.simpleweather.data.manager;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

import com.demo.simpleweather.SwApplication;
import com.demo.simpleweather.adapter.CityPagerAdapter;
import com.demo.simpleweather.data.City;
import com.demo.simpleweather.fragment.CityFragment_raw;
import com.demo.simpleweather.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class CityManager {
    private List<City> mCities;
    private List<CityFragment_raw> mCityFragmentRaws;

    private PagerAdapter mPagerAdapter;

    private static final String FileName = "Cities.dat";
    private static final int Capacity = 8;

    private boolean isSaved = false;

    private City tempCity;
    private int tempPosition;

    //TODO 考虑是否可以将CityManager设计成单例模式
    public CityManager() {
        restoreCities();
        initFragments();
    }

    //******************Override************

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        save();
    }


    //******************public**************

    /**
     * 保存数据到本地
     */
    public void save() {
        if (!isSaved) {
            isSaved = true;
            new Thread() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = SwApplication.getContext().openFileOutput(FileName, Context.MODE_PRIVATE);
                        ObjectOutputStream output = new ObjectOutputStream(outputStream);
                        output.writeObject(mCities);
                        output.close();

                        L.e(SwApplication.TAG, "保存 Cities : 保存成功");
                    } catch (IOException e) {
                        L.e(SwApplication.TAG, "保存 Cities : 保存失败");
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * 获取PagerAdapter
     *
     * @param fm FragmentManager
     * @return PagerAdapter
     */
    public PagerAdapter getPagerAdapter(FragmentManager fm) {
        mPagerAdapter = new CityPagerAdapter(fm, mCityFragmentRaws);
        return mPagerAdapter;
    }

    /**
     * 添加城市
     *
     * @param city 要添加的城市
     * @return 添加成功则返回true，否则返回false
     */
    public boolean addCity(City city) {
        if (city.isLocationCity()) {
            //调试
            L.d(SwApplication.TAG, "添加定位城市 : " + city.getName());

            //将原来的定位城市的设为false
            City nextCity = (City) mCityFragmentRaws.get(0).getArguments().getSerializable(CityFragment_raw.KEY_CITY);
            if (nextCity != null) {
                nextCity.setIsLocationCity(false);
                mCityFragmentRaws.get(0).getArguments().putSerializable(CityFragment_raw.KEY_CITY, nextCity);
            }

            return addCity(0, city);
        } else {
            //调试
            L.d(SwApplication.TAG, "添加普通城市 : " + city.getName());

            return addCity(mCities.size(), city);
        }
    }

    /**
     * 添加City到指定index处
     *
     * @param index 要添加到的位置
     * @param city  要添加的城市
     */
    public boolean addCity(int index, City city) {
        if (!contains(city) && !isFull()) {
            //调试
            L.d(SwApplication.TAG, "添加城市 : " + city.getName());

            mCities.add(index, city);
            CityFragment_raw page = new CityFragment_raw();
            Bundle args = new Bundle();
            args.putSerializable(CityFragment_raw.KEY_CITY, city);
            args.putInt(CityFragment_raw.KEY_POSITION, index);
            page.setArguments(args);
            mCityFragmentRaws.add(index, page);

            notifyDataChanged();
            return true;
        } else {
            //调试
            L.d(SwApplication.TAG, "添加城市失败 : " + city.getName()
                    + " 该城市已存在，或者容量已满");
            return false;
        }
    }

    /**
     * 删除指定位置的城市
     *
     * @param position 要删除的城市的位置
     */
    public boolean deleteCity(int position) {
        if (position > 0 || mCities.get(position).getName().equals("N/A")) {
            //调试
            L.d(SwApplication.TAG, "删除城市 : " + mCities.get(position).getName());

            tempCity = mCities.remove(position);    //缓存City
            tempPosition = position;                //缓存position

            mCityFragmentRaws.remove(position);
            notifyDataChanged();
            return true;
        }

        L.d(SwApplication.TAG, "删除城市失败 : " + mCities.get(position).getName()
                + " 该城市是HomePage");
        return false;
    }

    /**
     * 撤销删除，只能撤销上一次的删除，不能无限撤销
     * （本质上是将上一次删除的城市进行了重新添加）
     */
    public void undoDelete() {
        if (tempCity != null) {
            addCity(tempPosition, tempCity);
            tempCity = null;
        }
    }

    /**
     * 判断是否城市列表是否已满（最大容量：8）
     *
     * @return 如果已经满了，则返回true，否则返回false
     */
    public boolean isFull() {
        return mCities.size() == Capacity;
    }

    //******************private*************

    //从本地恢复数据
    private void restoreCities() {
        try {
            InputStream inputStream = SwApplication.getContext().openFileInput(FileName);
            ObjectInputStream input = new ObjectInputStream(inputStream);
            mCities = (LinkedList<City>) input.readObject();
            input.close();
            //调试
            L.d(SwApplication.TAG, "恢复 Cities : 恢复成功");
        } catch (IOException | ClassNotFoundException e) {
            mCities = new LinkedList<>();
            mCities.add(new City("N/A"));

            //调试
            L.d(SwApplication.TAG, "恢复 Cities : 恢复失败");
            e.printStackTrace();
        }
    }

    //初始化所有的Fragment
    private void initFragments() {
        mCityFragmentRaws = new LinkedList<>();
        for (int i = 0; i < mCities.size(); i++) {
            CityFragment_raw page = new CityFragment_raw();
            Bundle args = new Bundle();
            args.putSerializable(CityFragment_raw.KEY_CITY, mCities.get(i));
            args.putInt(CityFragment_raw.KEY_POSITION, i);
            page.setArguments(args);
            mCityFragmentRaws.add(page);
        }
    }

    //通知数据发生改变
    private void notifyDataChanged() {
        //调试
        L.d(SwApplication.TAG, "更新所有页面位置信息");
        for (int i = 0; i < mCityFragmentRaws.size(); i++) {
            mCityFragmentRaws.get(i)
                    .getArguments()
                    .putInt(CityFragment_raw.KEY_POSITION, i); //更新位置信息
        }
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    //判断某个城市是否已经存在
    private boolean contains(City city) {
        for (City i : mCities) {
            if (i.getName().equals(city.getName())) {
                return true;
            }
        }
        return false;
    }
}
