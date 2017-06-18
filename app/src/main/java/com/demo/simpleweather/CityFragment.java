package com.demo.simpleweather;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.simpleweather.utils.L;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CityFragment extends Fragment {
    public static final String KEY_CITY_NAME = "cityName";
    public static final String KEY_IS_HOME = "isLocation";
    public static final String KEY_POSITION = "position";

    private String cityName;
    private Weather weather;

    private long lastRefreshTime;  //用于记录上一次刷新的时间

    private MainActivity mainActivity;
    private View container;
    private View contentView;

    private TextView tvCityName;
    private TextView tvCurrentTmp;
    private TextView tvWeatherStatus;
    private TextView tvAirQuality;

    private ImageView ivLocationImage;

    private ImageButton ibHome;
    private ImageButton ibMenu;
    private ImageButton ibArrowUp;

    private SwipeRefreshLayout swipeRefresh;

    //*********************public***********************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cityName = getArguments().getString(KEY_CITY_NAME, "N/A");

        if (savedInstanceState != null) {
            L.d(WeatherApplication.TAG, "savedInstanceState");

            if (cityName.equals(savedInstanceState.getString("cityName"))) {
                weather = savedInstanceState.getParcelable("weather");
            } else {
                weather = new Weather();
            }

        } else {
            restoreWeatherFromDisk();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("cityName", cityName);
        outState.putParcelable("weather", weather);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.container = container;
        contentView = inflater.inflate(R.layout.weather_page, container, false);

        tvCityName = (TextView) contentView.findViewById(R.id.tvCityName);

        tvCurrentTmp = (TextView) contentView.findViewById(R.id.tvCurrentTmp);
        tvWeatherStatus = (TextView) contentView.findViewById(R.id.tvWeatherStatus);
        tvAirQuality = (TextView) contentView.findViewById(R.id.tvAirQuality);

        ivLocationImage = (ImageView) contentView.findViewById(R.id.ivLocationImage);

        ibHome = (ImageButton) contentView.findViewById(R.id.ibHome);
        ibMenu = (ImageButton) contentView.findViewById(R.id.ibMenu);
        ibArrowUp = (ImageButton) contentView.findViewById(R.id.ibArrowUp);

        swipeRefresh = (SwipeRefreshLayout) contentView.findViewById(R.id.swipeRefresh);

        L.d(WeatherApplication.TAG, "----------------------------");
        L.d(WeatherApplication.TAG, "City Name : " + cityName);
        L.d(WeatherApplication.TAG, "Position  : " + getPosition());
        L.d(WeatherApplication.TAG, "Is Home   : " + isHomePage());
        L.d(WeatherApplication.TAG, "----------------------------");

        initView();
        initViewListener();
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        refreshView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveWeatherData();
    }

    /**
     * 判断当前页面是否是主页面
     *
     * @return 如果当前页面是主页面，则返回true，否则返回false
     */
    public boolean isHomePage() {
        return getArguments().getBoolean(KEY_IS_HOME);
    }

    /**
     * 获取当前页面的位置
     *
     * @return 返回当前页面的位置
     */
    public int getPosition() {
        return getArguments().getInt(KEY_POSITION);
    }

    public boolean suggestRefreshWeather() {
        long currentTime = System.currentTimeMillis();
        long elapseMinute = (currentTime - lastRefreshTime) / 60000;

        L.d(WeatherApplication.TAG, "Elapse Minute : " + elapseMinute);

        if (elapseMinute >= 30) {
            refreshWeather();
            return true;
        }

        return false;
    }


    //**************************private*****************************

    //根据天气信息刷新视图
    private void refreshView() {
        L.d(WeatherApplication.TAG, cityName + " : refreshView");

        if (weather.isNA()) {
            L.d(WeatherApplication.TAG, cityName + " : Weather is NA");
            if (isHomePage() && !cityName.equals("N/A")) {
                refreshWeather();
            }
        } else {
            L.d(WeatherApplication.TAG, cityName + " : Weather Not NA");
            L.d(WeatherApplication.TAG, cityName + " : " + weather.getCurrentTmp());
            L.d(WeatherApplication.TAG, cityName + " : " + weather.getWeatherStatus());
            L.d(WeatherApplication.TAG, cityName + " : " + weather.getAirQualityString());
            tvCurrentTmp.setText(weather.getCurrentTmp());
            tvWeatherStatus.setText(weather.getWeatherStatus());
            tvAirQuality.setText(weather.getAirQualityString());

            container.setBackgroundResource(weather.getWeatherColorId());
            contentView.setBackgroundResource(weather.getWeatherColorId());
        }
    }


    //刷新天气
    private void refreshWeather() {
        lastRefreshTime = System.currentTimeMillis();

        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }

        L.d(WeatherApplication.TAG, "refreshWeather");

        OkHttpClient okHttpClient = WeatherApplication.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url("https://free-api.heweather.com/v5/weather?city=" + cityName + "&key=e7068f2051cf4ffd996ae643bd45d27d")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "刷新失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseString = response.body().string();
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weather.update(responseString);
                        Toast.makeText(getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                        refreshView();
                    }
                });
            }
        });
    }

    //保存天气信息到本地
    private void saveWeatherData() {
        if (!weather.isNA() && !cityName.equals("N/A")) {
            SharedPreferences.Editor save = getContext().getSharedPreferences(cityName, Context.MODE_PRIVATE).edit();

            save.putString("weatherStatus", weather.getWeatherStatus());
            save.putString("currentTmp", weather.getCurrentTmp());
            save.putInt("airQuality", weather.getAirQuality());

            save.putString("forecastWeather1", weather.getForecastWeather1());
            save.putString("forecastTmp1", weather.getForecastTmp1());
            save.putString("forecastWeather2", weather.getForecastWeather2());
            save.putString("forecastTmp2", weather.getForecastTmp2());

            save.putLong("lastRefreshTime", lastRefreshTime);

            save.apply();
        }
    }

    //从本地恢复天气信息
    private void restoreWeatherFromDisk() {
        weather = new Weather();

        L.d(WeatherApplication.TAG, cityName + " : restoreWeatherFromDisk");

        if (!cityName.equals("N/A")) {
            SharedPreferences savedData = getContext().getSharedPreferences(cityName, Context.MODE_PRIVATE);
            weather.setWeatherStatus(savedData.getString("weatherStatus", "N/A"));
            weather.setCurrentTmp(savedData.getString("currentTmp", "N/A"));
            weather.setAirQuality(savedData.getInt("airQuality", -1));

            weather.setForecastWeather1(savedData.getString("forecastWeather1", "N/A"));
            weather.setForecastTmp1(savedData.getString("forecastTmp1", "N/A"));
            weather.setForecastWeather2(savedData.getString("forecastWeather2", "N/A"));
            weather.setForecastTmp2(savedData.getString("forecastTmp2", "N/A"));

            lastRefreshTime = savedData.getLong("lastRefreshTime", 0);
        }
    }

    private void initView() {
        if (isHomePage()) {
            ivLocationImage.setVisibility(View.VISIBLE);
        } else {
            ibHome.setVisibility(View.VISIBLE);
            ibHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity.setPagePosition(0);
                }
            });
        }
        tvCityName.setText(cityName);
        container.setBackgroundResource(R.color.colorBlue);
    }

    private void initViewListener() {
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
                //TODO 待完成
            }
        });

        //向上箭头监听器
        ibArrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 待完成
            }
        });
    }

    /*
     TODO 菜单的删除城市功能（保留，请稍后加上）：
     mainActivity.setPagePosition(Math.max(0, getPosition() - 1));
     mainActivity.getCityManager().deleteCity(getArguments().getInt(KEY_POSITION));

     if (!isHomePage()) {
         Snackbar.make(mainActivity.findViewById(android.R.id.content), "删除中...", Snackbar.LENGTH_LONG)
                 .setAction("取消", new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         mainActivity.getCityManager().undoDelete();
                         mainActivity.setPagePosition(getPosition());
                     }
                 }).show();
     }
     */
}
