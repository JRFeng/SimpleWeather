package com.demo.simpleweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.simpleweather.utils.L;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/*
* 页面刷新功能正常
* 保存与恢复数据功能正常
* 跳转到Home Page功能正常
*/
public class CityFragment extends Fragment {
    private String cityName;
    private Weather weather;
    private boolean isHomePage;

    private View pageView;
    private ImageButton ibHome;
    private ImageButton ibMenu;
    private TextView tvCurrentTemp;
    private TextView tvWeatherStatus;
    private TextView tvAirQuality;
    private ImageButton ibBottomMenu;
    private SwipeRefreshLayout swipeRefresh;
    private PopupWindow optionMenu;

    private View menu;
    private Button menuAdd;
    private Button menuDelete;

    //***********************************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        cityName = arguments.getString("cityName");
        isHomePage = arguments.getBoolean("isHomePage");

        if (savedInstanceState != null) {
            weather = savedInstanceState.getParcelable("weather");
        } else {
            restoreFromDisk();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pageView = inflater.inflate(R.layout.weather_page, container, false);

        if (container != null) {
            container.setBackgroundResource(R.color.colorBlue);
        }

        ((TextView) pageView.findViewById(R.id.tvCityName)).setText(cityName);
        if (isHomePage) {
            pageView.findViewById(R.id.ivLocationImage).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.ibHome).setVisibility(View.VISIBLE);
        }

        ibHome = (ImageButton) pageView.findViewById(R.id.ibHome);
        ibMenu = (ImageButton) pageView.findViewById(R.id.ibMenu);
        tvCurrentTemp = (TextView) pageView.findViewById(R.id.tvCurrentTemp);
        tvWeatherStatus = (TextView) pageView.findViewById(R.id.tvWeather);
        tvAirQuality = (TextView) pageView.findViewById(R.id.tvAirQuality);
        ibBottomMenu = (ImageButton) pageView.findViewById(R.id.ibBottomMenu);
        swipeRefresh = (SwipeRefreshLayout) pageView.findViewById(R.id.swipeRefresh);

        menu = inflater.inflate(R.layout.option_menu, container, false);
        menuAdd = (Button) menu.findViewById(R.id.addCity);
        menuDelete = (Button) menu.findViewById(R.id.deleteCity);
        optionMenu = new PopupWindow(menu, 128 * WApplication.getInstance().getDPI() / 160, WRAP_CONTENT);

        initViews(weather);
        addViewListener();
        return pageView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (weather.isNA()){
//            refreshWeather();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("weather", weather);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveToDisk();
    }

    public void refreshWeather() {
        if (cityName.equals("N/A")) {
            swipeRefresh.setRefreshing(false);
            return;
        }
        final String url = "https://free-api.heweather.com/v5/weather?city=" + cityName + "&key=e7068f2051cf4ffd996ae643bd45d27d";
        OkHttpClient okHttpClient = WApplication.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "刷新失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                String responseData = responseBody.string();
                L.d("App", responseData);
                weather.update(responseData);

                //更新UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        initViews(weather);
                        Toast.makeText(getContext(), "已更新", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //***********************************************************************

    private void initViews(Weather weather) {
        tvCurrentTemp.setText(weather.getCurrentTemp());
        tvWeatherStatus.setText(weather.getWeatherStatus());
        tvAirQuality.setText(weather.getAirQualityString());
    }

    private void addViewListener() {
        if (!isHomePage) {
            ibHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    optionMenu.dismiss();
                    ((ViewPager) getActivity().findViewById(R.id.vpContainer)).setCurrentItem(0, true);
                }
            });
        }

        ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!optionMenu.isShowing()) {
                    optionMenu.showAsDropDown(ibMenu, -100 * WApplication.getInstance().getDPI() / 160, 0, Gravity.CENTER);
                } else {
                    optionMenu.dismiss();
                }
                L.d("App", "onClick");
            }
        });
        pageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                optionMenu.dismiss();
                return false;
            }
        });

        ibBottomMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 从底部弹出未来两天的天气预报
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWeather();
            }
        });

        menuDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionMenu.dismiss();
                Snackbar.make(ibBottomMenu, "删除中...", Snackbar.LENGTH_LONG)
                        .setAction("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getContext(), "已取消", Toast.LENGTH_SHORT).show();
                            }
                        }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        CityManager.getInstance().deleteCity(cityName);
                        Toast.makeText(WApplication.getContext(), "已删除", Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
    }

    private void saveToDisk() {
        if (weather != null && !cityName.equals("N/A")) {
            SharedPreferences.Editor weatherData = getContext().getSharedPreferences(cityName + ".dat", Context.MODE_PRIVATE).edit();

            weatherData.putString("weather", weather.getWeatherStatus());
            weatherData.putString("currentTemp", weather.getCurrentTemp());
            weatherData.putInt("airQuality", weather.getAirQuality());
            weatherData.putString("forecastWeather1", weather.getForecastWeather1());
            weatherData.putString("forecastTemp1", weather.getForecastTemp1());
            weatherData.putString("forecastWeather2", weather.getForecastWeather2());
            weatherData.putString("forecastTemp2", weather.getForecastTemp2());

            weatherData.apply();
        }
    }

    private void restoreFromDisk() {
        weather = new Weather();
        if (!cityName.equals("N/A")) {
            SharedPreferences savedData = getContext().getSharedPreferences(cityName + ".dat", Context.MODE_PRIVATE);

            weather.setWeatherStatus(savedData.getString("weather", "N/A"));
            weather.setCurrentTemp(savedData.getString("currentTemp", "N/A"));
            weather.setAirQuality(savedData.getInt("airQuality", Weather.AIR_UNKNOWN));
            weather.setForecastWeather1(savedData.getString("forecastWeather1", "N/A"));
            weather.setForecastTemp1(savedData.getString("forecastTemp1", "N/A"));
            weather.setForecastWeather2(savedData.getString("forecastWeather2", "N/A"));
            weather.setForecastTemp2(savedData.getString("forecastTemp2", "N/A"));
        }
    }

    public String getCityName() {
        return cityName;
    }
}
