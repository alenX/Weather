package org.alenx.weather.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.alenx.weather.DBUtils.WeatherDBHelp;
import org.alenx.weather.Models.City;
import org.alenx.weather.Models.County;
import org.alenx.weather.Models.OfflineCounty;
import org.alenx.weather.Models.Province;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    /*处理从服务器返回的省信息*/
    public synchronized static boolean handleProvince(WeatherDBHelp db, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allPros = response.split(",");
            if (allPros.length > 0) {
                for (String p : allPros) {
                    String[] pInfo = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(pInfo[0]);
                    province.setProvinceName(pInfo[1]);
                    db.saveProvince(province);
                }
            }
            return true;
        }
        return false;
    }

    /*处理从服务器返回的市信息*/
    public synchronized static boolean handleCity(WeatherDBHelp db, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities.length > 0) {
                for (String c : allCities) {
                    String[] cityObj = c.split("\\|");
                    City city = new City();
                    city.setCityCode(cityObj[0]);
                    city.setCityName(cityObj[1]);
                    city.setProvinceId(provinceId);
                    db.saveCity(city);
                }
            }
            return true;
        }
        return false;
    }


    /*返回的县信息*/
    public synchronized static boolean handleCounty(WeatherDBHelp db, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if ( allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] countyObj = c.split("\\|");
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyCode(countyObj[0]);
                    county.setCountyName(countyObj[1]);
                    //TODO 可以将天气信息放置到该表中，即 WeatherCode
                    db.saveCounty(county);
                }
            }
            return true;
        }
        return false;
    }

    public static void handleWeatherResponse(Context context, String response, WeatherDBHelp db, String countyCode) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String img1 = weatherInfo.getString("img1");
            String img2 = weatherInfo.getString("img2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime, countyCode,img1,img2);


            String weatherImg1 =weatherInfo.getString("img1");
            String weatherImg2 =weatherInfo.getString("img2");
            HttpUtils.downWeatherPicture("http://m.weather.com.cn/img/"+weatherImg1,"Test",weatherImg1);
            HttpUtils.downWeatherPicture("http://m.weather.com.cn/img/"+weatherImg2,"Test",weatherImg2);

            /*同时将信息保存到离线城市信息表中*/
            OfflineCounty offlineCounty = new OfflineCounty();
            offlineCounty.setCityId(weatherInfo.getString("cityid"));
            offlineCounty.setWeatherInfo(weatherInfo);
            offlineCounty.setCountyCode(countyCode);
            SimpleDateFormat sdf = new SimpleDateFormat("M月d日", Locale.CHINA);
            offlineCounty.setLastUpdateTime(sdf.format(new Date()));
            db.saveOfflineCounty(offlineCounty);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public static void handleWeatherOffline(Context context, String response, WeatherDBHelp db, String countyCode) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String img1 = weatherInfo.getString("img1");
            String img2 = weatherInfo.getString("img2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime, countyCode,img1,img2);

            /*同时将信息保存到离线城市信息表中*//*
            OfflineCounty offlineCounty = new OfflineCounty();
            offlineCounty.setCityId(weatherInfo.getString("cityid"));
            offlineCounty.setWeatherInfo(weatherInfo);
            offlineCounty.setCountyCode(countyCode);
            SimpleDateFormat sdf = new SimpleDateFormat("M月d日", Locale.CHINA);
            offlineCounty.setLastUpdateTime(sdf.format(new Date()));
            db.saveOfflineCounty(offlineCounty);*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
                                       String temp1, String temp2, String weatherDesp, String publishTime, String countyCode,String img1,String img2) {


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("img1", img1);
        editor.putString("img2", img2);
        editor.putString("weatherDesp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.putString("county_code", countyCode);
        editor.commit();
    }
}
