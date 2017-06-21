package com.demo.simpleweather.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.simpleweather.R;
import com.demo.simpleweather.SwApplication;
import com.demo.simpleweather.activity.AddCityActivity;
import com.demo.simpleweather.activity.MainActivity;
import com.demo.simpleweather.data.City;
import com.demo.simpleweather.data.Weather;
import com.demo.simpleweather.utils.L;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CityFragment extends Fragment {
    private City mCity;
    private Weather mWeather;
    private long mLastRefreshTime;

    private MainActivity mActivity;
    private View mPageView;

    private TextView tvCityName;
    private TextView tvCurrentTmp;
    private TextView tvWeatherStatus;
    private TextView tvAirQuality;

    private ImageView ivLocationImage;

    private ImageButton ibHome;
    private ImageButton ibMenu;
    private ImageButton ibArrowUp;

    private SwipeRefreshLayout swipeRefresh;

    private PopupWindow pwMenu;
    private View pwMenuView;

    private PopupWindow pwForecast;
    private View pwForecastView;
    private TextView tvForecastWeather1;
    private TextView tvForecastTmp1;
    private TextView tvForecastWeather2;
    private TextView tvForecastTmp2;

    //********************************Override**********************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPageView = inflater.inflate(R.layout.weather_page, container, false);

        tvCityName = (TextView) mPageView.findViewById(R.id.tvCityName);

        tvCurrentTmp = (TextView) mPageView.findViewById(R.id.tvCurrentTmp);
        tvWeatherStatus = (TextView) mPageView.findViewById(R.id.tvWeatherStatus);
        tvAirQuality = (TextView) mPageView.findViewById(R.id.tvAirQuality);

        ivLocationImage = (ImageView) mPageView.findViewById(R.id.ivLocationImage);

        ibHome = (ImageButton) mPageView.findViewById(R.id.ibHome);
        ibMenu = (ImageButton) mPageView.findViewById(R.id.ibMenu);
        ibArrowUp = (ImageButton) mPageView.findViewById(R.id.ibArrowUp);

        swipeRefresh = (SwipeRefreshLayout) mPageView.findViewById(R.id.swipeRefresh);

        addViewListener();
        return mPageView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        initView();
        refreshView();
        suggestRefreshWeather();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData();
    }

    //************************public**********************

    public City getCity() {
        return mCity;
    }

    public void setCity(City mCity) {
        this.mCity = mCity;
    }

    public boolean isHome() {
        return mActivity.getPosition(mCity) == 0;
    }

    public boolean isLocation() {
        return mCity.isLocationCity();
    }

    public boolean suggestRefreshWeather() {

        /*
         * 当前if语句和 MainActivity.setIndicatorColorRes() 方
         * 法的耦合度很高，下次重构时请注意！
         */
        if(mActivity.getCurrentPosition() == mActivity.getPosition(mCity)){
            mActivity.setIndicatorColorRes(mWeather.getWeatherDarkColorId());
        }

        long elapseMinute = getElapseMinute();

        L.d(SwApplication.TAG, "Elapse Minute : " + elapseMinute);

        if ((elapseMinute >= 15 || mWeather.isNA()) && !mCity.getName().equals("N/A")) {
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 建议刷新 : 接受");
            L.d(SwApplication.TAG, mCity.getName() + " : Elapse  : " + elapseMinute);
            L.d(SwApplication.TAG, mCity.getName() + " : IsNa    : " + mWeather.isNA());

            refreshWeather();
            return true;
        }
        //调试
        L.d(SwApplication.TAG, mCity.getName() + " : 建议刷新 : 拒绝");
        return false;
    }

    //*************************private**********************

    private void initView() {
        //调试
        L.d(SwApplication.TAG, mCity.getName() + " : initView");

        if (isLocation()) {
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 当前定位城市");

            ivLocationImage.setVisibility(View.VISIBLE);
        } else if (isHome()) {
            //什么也不做
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 当前主页城市（非定位城市）");
        } else {
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 一般城市");

            ibHome.setVisibility(View.VISIBLE);
            ibHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.setCurrentPage(0, true);
                }
            });
        }
        tvCityName.setText(mCity.getName());

        //调试
        L.d(SwApplication.TAG, mCity.getName() + " : 页面位置 : " + mActivity.getPosition(mCity));
    }

    private void addViewListener() {
        //下拉刷新监听器
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWeather();
            }
        });

        //菜单监听器
        ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pwMenu == null) {
                    pwMenuView = LayoutInflater.from(getContext()).inflate(R.layout.popup_window_menu, (ViewGroup) mPageView, false);
                    pwMenu = new PopupWindow(
                            pwMenuView,
                            SwApplication.getInstance().getPx(128),
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    pwMenu.setTouchable(true);
                    pwMenu.setFocusable(true);
                    pwMenu.setBackgroundDrawable(new BitmapDrawable());
                    pwMenu.setOutsideTouchable(true);

                    //添加城市
                    pwMenuView.findViewById(R.id.menuAdd).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pwMenu.dismiss();
                            Intent intent = new Intent(getContext(), AddCityActivity.class);
                            intent.putExtra("weatherColorId", mWeather.getWeatherColorId());
                            mActivity.startActivityForResult(intent, 0);
                        }
                    });

                    //删除城市
                    pwMenuView.findViewById(R.id.menuDelete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pwMenu.dismiss();
                            mActivity.deleteCity(mCity);

                            if (!isHome()) {
                                Snackbar.make(mActivity.findViewById(R.id.vpContainer), "删除中...", Snackbar.LENGTH_SHORT)
                                        .setAction("取消", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                mActivity.undoDelete();
                                                mActivity.setCurrentPage(mActivity.getPosition(mCity), true);
                                                Toast.makeText(SwApplication.getContext(), "取消成功", Toast.LENGTH_SHORT).show();
                                            }
                                        }).show();
                            }
                        }
                    });
                }
                pwMenuView.setBackgroundResource(mWeather.getWeatherDarkColorId());
                pwMenu.showAsDropDown(view);
            }
        });

        //向上箭头监听器
        ibArrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pwForecast == null) {
                    pwForecastView = LayoutInflater.from(getContext())
                            .inflate(R.layout.popup_window_forecast, (ViewGroup) mPageView, false);

                    pwForecast = new PopupWindow(pwForecastView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    pwForecast.setTouchable(true);
                    pwForecast.setFocusable(true);
                    pwForecast.setBackgroundDrawable(new BitmapDrawable());
                    pwForecast.setOutsideTouchable(true);
                    pwForecast.setAnimationStyle(R.style.popupAnim);

                    tvForecastWeather1 = (TextView) pwForecastView.findViewById(R.id.tvForecastWeather1);
                    tvForecastTmp1 = (TextView) pwForecastView.findViewById(R.id.tvForecastTmp1);
                    tvForecastWeather2 = (TextView) pwForecastView.findViewById(R.id.tvForecastWeather2);
                    tvForecastTmp2 = (TextView) pwForecastView.findViewById(R.id.tvForecastTmp2);
                }

                tvForecastWeather1.setText(mWeather.getForecastWeather1());
                tvForecastTmp1.setText(mWeather.getForecastTmp1());
                tvForecastWeather2.setText(mWeather.getForecastWeather2());
                tvForecastTmp2.setText(mWeather.getForecastTmp2());

                pwForecastView.setBackgroundResource(mWeather.getWeatherDarkColorId());
                pwForecast.showAtLocation(mPageView, Gravity.BOTTOM, 0, 0);
            }
        });
    }

    //保存天气信息到本地
    private void saveData() {
        if (!mWeather.isNA() && !mCity.getName().equals("N/A")) {
            SharedPreferences.Editor save = getContext().getSharedPreferences(mCity.getName(), Context.MODE_PRIVATE).edit();

            save.putString("weatherStatus", mWeather.getWeatherStatus());
            save.putString("currentTmp", mWeather.getCurrentTmp());
            save.putInt("airQuality", mWeather.getAirQuality());

            save.putString("forecastWeather1", mWeather.getForecastWeather1());
            save.putString("forecastTmp1", mWeather.getForecastTmp1());
            save.putString("forecastWeather2", mWeather.getForecastWeather2());
            save.putString("forecastTmp2", mWeather.getForecastTmp2());

            save.putLong("lastRefreshTime", mLastRefreshTime);

            save.apply();

            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 保存数据到本地 : 成功");
        } else {//调试else
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 保存数据到本地 : 失败");
        }
    }

    //从本地恢复天气信息
    private void restoreData() {
        mWeather = new Weather();

        if (!mCity.getName().equals("N/A")) {
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 从本地恢复页面数据 : 接受");

            SharedPreferences savedData = getContext().getSharedPreferences(mCity.getName(), Context.MODE_PRIVATE);
            mWeather.setWeatherStatus(savedData.getString("weatherStatus", "N/A"));
            mWeather.setCurrentTmp(savedData.getString("currentTmp", "N/A"));
            mWeather.setAirQuality(savedData.getInt("airQuality", -1));

            mWeather.setForecastWeather1(savedData.getString("forecastWeather1", "N/A"));
            mWeather.setForecastTmp1(savedData.getString("forecastTmp1", "N/A"));
            mWeather.setForecastWeather2(savedData.getString("forecastWeather2", "N/A"));
            mWeather.setForecastTmp2(savedData.getString("forecastTmp2", "N/A"));

            mLastRefreshTime = savedData.getLong("lastRefreshTime", 0);
        } else {//调试else
            //调试
            L.d(SwApplication.TAG, mCity.getName() + " : 从本地恢复页面数据 : 拒绝");
        }
    }

    //刷新天气
    private void refreshWeather() {
        if (mActivity.getCurrentPosition() != mActivity.getPosition(mCity)) {
            L.d(SwApplication.TAG, mCity.getName() + " : 拒绝刷新");
            return;
        }

        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }

        String cityName = mCity.getName();

        //调试
        L.d(SwApplication.TAG, cityName + " : refreshWeather : 接受刷新");
        L.d(SwApplication.TAG, cityName + " : Current position : " + mActivity.getCurrentPosition());
        L.d(SwApplication.TAG, cityName + " : position : " + mActivity.getPosition(mCity));

        OkHttpClient okHttpClient = SwApplication.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url("https://free-api.heweather.com/v5/weather?city=" + cityName + "&key=e7068f2051cf4ffd996ae643bd45d27d")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), mCity.getName() + "：更新失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        if (mWeather.update(responseString)) {
                            Toast.makeText(mActivity, mCity.getName() + "：已更新", Toast.LENGTH_SHORT).show();
                            refreshView();
                            mLastRefreshTime = System.currentTimeMillis();

                            /*
                             * 当前if语句和 MainActivity.setIndicatorColorRes() 方
                             * 法的耦合度很高，下次重构时请注意！
                             */
                            if(mActivity.getCurrentPosition() == mActivity.getPosition(mCity)){
                                mActivity.setIndicatorColorRes(mWeather.getWeatherDarkColorId());
                            }

                            //调试
                            L.d(SwApplication.TAG, "刷新成功 : " + mCity.getName());
                            L.d(SwApplication.TAG, "Elapse Minute : " + getElapseMinute());
                        } else {
                            Toast.makeText(mActivity, mCity.getName() + "：更新失败", Toast.LENGTH_SHORT).show();

                            //调试
                            L.d(SwApplication.TAG, "刷新失败 : status 异常 : " + mCity.getName());
                        }
                    }
                });
            }
        });
    }

    //根据天气信息刷新视图
    private void refreshView() {
        //调试
        L.d(SwApplication.TAG, mCity.getName() + " : 刷新View");

        tvCurrentTmp.setText(mWeather.getCurrentTmp());
        tvWeatherStatus.setText(mWeather.getWeatherStatus());
        tvAirQuality.setText(mWeather.getAirQualityString());

        swipeRefresh.setColorSchemeResources(mWeather.getWeatherDarkColorId());

        refreshBgColor();
    }

    private long getElapseMinute() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - mLastRefreshTime) / 60000;
    }

    //刷新背景颜色
    private void refreshBgColor() {
        mPageView.setBackgroundResource(mWeather.getWeatherColorId());
    }
}
