package com.demo.simpleweather.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.demo.simpleweather.fragment.CityFragment;

import java.util.List;


public class CityPagerAdapter extends FragmentStatePagerAdapter {
    private List<CityFragment> mCityFragments;

    public CityPagerAdapter(FragmentManager fm, List<CityFragment> cityFragments) {
        super(fm);
        mCityFragments = cityFragments;
    }

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
}