package com.demo.simpleweather;

import android.os.Parcel;
import android.os.Parcelable;

import com.demo.simpleweather.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Weather implements Parcelable {
    private String weatherStatus;
    private String currentTemp;
    private int airQuality;

    private String forecastWeather1;
    private String forecastTemp1;
    private String forecastWeather2;
    private String forecastTemp2;

    public static final int AIR_GOOD = 0;
    public static final int AIR_FINE = 1;
    public static final int AIR_SLIGHT_POLLUTION = 2;
    public static final int AIR_MODERATE_POLLUTION = 3;
    public static final int AIR_HEAVY_POLLUTION = 4;
    public static final int AIR_SEVERE_POLLUTION = 5;
    public static final int AIR_UNKNOWN = -1;

    public Weather() {
        weatherStatus = "N/A";
        currentTemp = "N/A";
        airQuality = AIR_UNKNOWN;

        forecastWeather1 = "N/A";
        forecastTemp1 = "N/A";
        forecastWeather2 = "N/A";
        forecastTemp2 = "N/A";
    }

//    public Weather(String weatherStatus, String currentTemp, String minTemp, int airQuality,
//                   String forecastWeather1, String forecastTemp1, String forecastWeather2, String forecastTemp2) {
//        this.weatherStatus = weatherStatus;
//        this.currentTemp = currentTemp;
//        this.minTemp = minTemp + "\u00B0";
//        this.airQuality = airQuality;
//
//        this.forecastWeather1 = forecastWeather1;
//        this.forecastTemp1 = forecastTemp1;
//        this.forecastWeather2 = forecastWeather2;
//        this.forecastTemp2 = forecastTemp2;
//    }

    protected Weather(Parcel in) {
        weatherStatus = in.readString();
        currentTemp = in.readString();
        airQuality = in.readInt();
        forecastWeather1 = in.readString();
        forecastTemp1 = in.readString();
        forecastWeather2 = in.readString();
        forecastTemp2 = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(weatherStatus);
        parcel.writeString(currentTemp);
        parcel.writeInt(airQuality);
        parcel.writeString(forecastWeather1);
        parcel.writeString(forecastTemp1);
        parcel.writeString(forecastWeather2);
        parcel.writeString(forecastTemp2);
    }

    public static final Creator<Weather> CREATOR = new Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel in) {
            return new Weather(in);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };


    public String getWeatherStatus() {
        return weatherStatus;
    }

    public void setWeatherStatus(String weatherStatus) {
        this.weatherStatus = weatherStatus;
    }

    public String getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(String currentTemp) {
        this.currentTemp = currentTemp;
    }


    public int getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(int airQuality) {
        this.airQuality = airQuality;
    }

    public String getForecastWeather1() {
        return forecastWeather1;
    }

    public void setForecastWeather1(String forecastWeather1) {
        this.forecastWeather1 = forecastWeather1;
    }

    public String getForecastTemp1() {
        return forecastTemp1;
    }

    public void setForecastTemp1(String forecastTemp1) {
        this.forecastTemp1 = forecastTemp1;
    }

    public String getForecastWeather2() {
        return forecastWeather2;
    }

    public void setForecastWeather2(String forecastWeather2) {
        this.forecastWeather2 = forecastWeather2;
    }

    public String getForecastTemp2() {
        return forecastTemp2;
    }

    public void setForecastTemp2(String forecastTemp2) {
        this.forecastTemp2 = forecastTemp2;
    }

    public String getAirQualityString() {
        String str;
        switch (getAirQuality()) {
            case AIR_GOOD:
                str = "空气质量: 优";
                break;
            case AIR_FINE:
                str = "空气质量: 良";
                break;
            case AIR_SLIGHT_POLLUTION:
                str = "空气质量: 轻度污染";
                break;
            case AIR_MODERATE_POLLUTION:
                str = "空气质量: 中度污染";
                break;
            case AIR_HEAVY_POLLUTION:
                str = "空气质量: 重度污染";
                break;
            case AIR_SEVERE_POLLUTION:
                str = "空气质量: 严重污染";
                break;
            default:
                str = "空气质量: N/A";
                break;
        }
        return str;
    }

    public void update(String json) {
        L.d("App", "Update Weather");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray heWeather5 = jsonObject.getJSONArray("HeWeather5");
            JSONObject weatherDetails = heWeather5.getJSONObject(0);

            this.airQuality = decodeAirStatus(weatherDetails.getJSONObject("aqi").getJSONObject("city").getString("qlty"));
            L.d("App", "airQuality : " + airQuality);

            JSONObject now = weatherDetails.getJSONObject("now");
            this.weatherStatus = now.getJSONObject("cond").getString("txt");
            L.d("App", "weatherStatus : " + weatherStatus);

            this.currentTemp = now.getString("tmp");

        } catch (JSONException e) {
            L.e("App", "Update Error");
            e.printStackTrace();
        }
    }

    private int decodeAirStatus(String airStatus){
        int statusCode = AIR_UNKNOWN;
        switch (airStatus) {
            case "优":
                statusCode = AIR_GOOD;
                break;
            case "良":
                statusCode = AIR_FINE;
                break;
            case "轻度污染":
                statusCode = AIR_SLIGHT_POLLUTION;
                break;
            case "中度污染":
                statusCode = AIR_MODERATE_POLLUTION;
                break;
            case "重度污染":
                statusCode = AIR_HEAVY_POLLUTION;
                break;
            case "严重污染":
                statusCode = AIR_SEVERE_POLLUTION;
                break;
        }
        return statusCode;
    }

    public boolean isNA() {
        String[] array = {weatherStatus, currentTemp, forecastWeather1, forecastTemp1, forecastWeather2, forecastTemp2};
        for (String i : array) {
            if (i.equals("N/A")) {
                return true;
            }
        }
        return false;
    }
}
