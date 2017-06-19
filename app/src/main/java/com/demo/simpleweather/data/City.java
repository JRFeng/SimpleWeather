package com.demo.simpleweather.data;

import java.io.Serializable;


public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mCityName;
    private boolean mIsLocationCity;

    public City(String cityName) {
        this(cityName, false);
    }

    public City(String cityName, boolean isLocationCity) {
        mCityName = cityName;
        mIsLocationCity = isLocationCity;
    }

    public String getName() {
        return mCityName;
    }

    public void setName(String cityName) {
        mCityName = cityName;
    }

    public boolean isLocationCity() {
        return mIsLocationCity;
    }

    public void setIsLocationCity(boolean isLocationCity) {
        mIsLocationCity = isLocationCity;
    }

}
