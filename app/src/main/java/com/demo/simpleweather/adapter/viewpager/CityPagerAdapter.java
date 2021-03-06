package com.demo.simpleweather.adapter.viewpager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.demo.simpleweather.SWApplication;
import com.demo.simpleweather.data.City;
import com.demo.simpleweather.fragment.CityFragment;
import com.demo.simpleweather.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;


public class CityPagerAdapter extends FragmentStatePagerAdapter {
    private List<City> mCities;
    private List<CityFragment> mCityFragments;

    private static final String FileName = "Cities.dat";
    private static final int Capacity = 8;

    private boolean isSaved = false;

    private City tempCity;
    private int tempPosition;

    /*
     * 警告，不要将CityPagerAdapter设计成单例，这会导致严重的BUG!!!
     * 因为单例会重用已销毁的Fragment，而已销毁的Fragment是没有UI界面的，
     * 因此会导致白屏。
     */

    //********************Constructor*********************

    public CityPagerAdapter(FragmentManager fm) {
        super(fm);
        restoreCities();
        initFragments();
    }


    //********************Override******************

    @Override
    public Fragment getItem(int position) {
        return mCityFragments.get(position);
    }

    @Override
    public int getCount() {
        return mCityFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        save();
    }

    //*********************Public********************

    /**
     * 添加城市
     *
     * @param city 要添加的城市
     * @return 添加成功则返回true，否则返回false
     */
    public boolean addCity(City city) {
        if (city.isLocationCity()) {  //判断是否是定位城市
            //调试
            L.d(SWApplication.TAG, "添加定位城市 : " + city.getName());

            if (contains(city)) {  //判断定位城市是否已存在
                L.d(SWApplication.TAG, "定位城市 : " + city.getName() + " : 已存在");
                if (getPosition(city) == 0) {  //判断定位城市是否是HomePage
                    L.d(SWApplication.TAG, "定位城市已存在 : " + city.getName() + " : 是HomePage，不添加");
                    return false;
                } else {
                    //如果不是HomePage，那么删除重新添加
                    deleteCity(city);
                    L.d(SWApplication.TAG, "定位城市已存在 : " + city.getName() + " : 不是HomePage，删除后重新添加");
                }
            }

            L.d(SWApplication.TAG, "定位城市 : " + city.getName() + " : 不存在");

            //将原来的定位城市的设为false
            City nextCity = mCityFragments.get(0).getCity();
            if (nextCity.getName().equals("N/A")) {
                deleteCity(nextCity);
            } else {
                nextCity.setIsLocationCity(false);
            }

            return addCity(0, city);
        } else {
            //调试
            L.d(SWApplication.TAG, "添加普通城市 : " + city.getName());
            if (contains(new City("N/A"))) {
                deleteCity(new City("N/A"));
            }
            return addCity(mCities.size(), city);
        }
    }

    /**
     * 添加City到指定index处
     *
     * @param index 要添加到的位置
     * @param city  要添加的城市
     */
    private boolean addCity(int index, City city) {
        if (!contains(city) && !isFull()) {
            //调试
            L.d(SWApplication.TAG, "添加城市 : " + city.getName());

            mCities.add(index, city);
            CityFragment page = new CityFragment();
            page.setCity(city);
            mCityFragments.add(index, page);

            notifyDataChanged();

            //调试
            L.d(SWApplication.TAG, "添加城市成功 : " + city.getName());
            return true;
        } else {
            if (isFull()) {
                //调试
                L.d(SWApplication.TAG, "添加城市失败 : " + city.getName() + " : 容量已满");
            }
            if (contains(city)) {
                //调试
                L.d(SWApplication.TAG, "添加城市失败 : " + city.getName() + " : 已经存在");
            }
            return false;
        }
    }

    /**
     * 删除指定的城市
     *
     * @param city 要删除的城市
     */
    public boolean deleteCity(City city) {
        int position = getPosition(city);
        if (position > 0 || mCities.get(position).getName().equals("N/A")) {
            //调试
            L.d(SWApplication.TAG, "删除城市 : " + mCities.get(position).getName());

            tempCity = mCities.remove(position);    //缓存City
            tempPosition = position;                //缓存position

            mCityFragments.remove(position);
            notifyDataChanged();
            return true;
        }

        L.d(SWApplication.TAG, "删除城市失败 : " + mCities.get(position).getName()
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

    public int getPosition(City city) {
        for (int i = 0; i < mCities.size(); i++) {
            if (city.getName().equals(mCities.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    //判断某个城市是否已经存在
    public boolean contains(City city) {
        for (City i : mCities) {
            if (i.getName().equals(city.getName())) {
                return true;
            }
        }
        return false;
    }

    public void suggestRefreshWeather(int position) {
        mCityFragments.get(position).suggestRefreshWeather();
    }

    public int getSize() {
        return mCities.size();
    }

    public String[] getCityNames() {
        String[] cities = new String[mCities.size()];
        for (int i = 0; i < mCities.size(); i++) {
            cities[i] = mCities.get(i).getName();
        }
        return cities;
    }

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
                        OutputStream outputStream = SWApplication.getContext().openFileOutput(FileName, Context.MODE_PRIVATE);
                        ObjectOutputStream output = new ObjectOutputStream(outputStream);
                        output.writeObject(mCities);
                        output.close();

                        L.e(SWApplication.TAG, "保存 Cities : 保存成功");
                    } catch (IOException e) {
                        L.e(SWApplication.TAG, "保存 Cities : 保存失败");
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }


    //*********************Private*******************

    //从本地恢复数据
    private void restoreCities() {
        try {
            InputStream inputStream = SWApplication.getContext().openFileInput(FileName);
            ObjectInputStream input = new ObjectInputStream(inputStream);
            mCities = (LinkedList<City>) input.readObject();
            input.close();
            //调试
            L.d(SWApplication.TAG, "恢复 Cities : 恢复成功");
        } catch (IOException | ClassNotFoundException e) {
            mCities = new LinkedList<>();
            mCities.add(new City("N/A"));

            //调试
            L.d(SWApplication.TAG, "恢复 Cities : 恢复失败");
            e.printStackTrace();
        }

        for (City i : mCities) {
            //调试
            L.d(SWApplication.TAG, i.getName());
        }
    }

    //初始化所有的Fragment
    private void initFragments() {
        mCityFragments = new LinkedList<>();
        for (int i = 0; i < mCities.size(); i++) {
            CityFragment page = new CityFragment();
            page.setCity(mCities.get(i));
            mCityFragments.add(page);
        }
    }

    //通知数据发生改变
    private void notifyDataChanged() {
        //调试
        L.d(SWApplication.TAG, "notifyDataSetChanged");

        this.notifyDataSetChanged();
    }
}