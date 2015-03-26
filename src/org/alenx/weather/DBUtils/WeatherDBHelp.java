package org.alenx.weather.DBUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.alenx.weather.Models.*;
import org.alenx.weather.Utils.Annos.Column;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class WeatherDBHelp {
    public static final String DB_NAME = "weather_db";

    public static final int VERSION = 1;

    private static WeatherDBHelp weatherDBHelp;

    private SQLiteDatabase db;

    private WeatherDBHelp(Context context) {
        WeatherDBOpenHelp dbOpenHelp = new WeatherDBOpenHelp(context, DB_NAME, null, VERSION);
        db = dbOpenHelp.getWritableDatabase();
    }

    public synchronized static WeatherDBHelp getInstance(Context context) {
        if (weatherDBHelp == null) {
            weatherDBHelp = new WeatherDBHelp(context);
        }
        return weatherDBHelp;
    }

    public ArrayList<Province> loadProvince() {
        ArrayList<Province> arrayList = new ArrayList<Province>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province p = new Province();
                //TODO
                p.setId(cursor.getInt(cursor.getColumnIndex("id")));
                p.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                p.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                arrayList.add(p);

            } while (cursor.moveToNext());
        }

        return arrayList;
    }

    public ArrayList<City> loadCity(int provinceId) {
        ArrayList<City> arrayList = new ArrayList<City>();
        Cursor cursor = db.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City c = new City();
                //TODO
                c.setId(cursor.getInt(cursor.getColumnIndex("id")));
                c.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                c.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                c.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
                arrayList.add(c);

            } while (cursor.moveToNext());
        }

        return arrayList;
    }

    public OfflineCounty getOfflineCounty(String countyCode) {
        Cursor cursor = db.query("OfflineCounty", null, "county_code = ?", new String[]{String.valueOf(countyCode)}, null, null, null);
        OfflineCounty c = null;
        if (cursor.moveToFirst()) {
            do {
                c = new OfflineCounty();
                //TODO
                c.setId(cursor.getInt(cursor.getColumnIndex("id")));
                c.setCityId(cursor.getString(cursor.getColumnIndex("city_id")));
                c.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                try {
                    c.setWeatherInfo(new JSONObject(cursor.getString(cursor.getColumnIndex("weather_info"))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                c.setLastUpdateTime(cursor.getString(cursor.getColumnIndex("last_update_time")));


            } while (cursor.moveToNext());
        }
        return c;
    }

    public ArrayList<County> loadCounty(int cityId) {
        ArrayList<County> arrayList = new ArrayList<County>();
        Cursor cursor = db.query("County", null, "city_id=?", new String[]{String.valueOf(cityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County c = new County();
                //TODO
                c.setId(cursor.getInt(cursor.getColumnIndex("id")));
                c.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
                c.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                c.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                arrayList.add(c);

            } while (cursor.moveToNext());
        }

        return arrayList;
    }

    /*加载收藏夹信息*/
    public ArrayList<CacheCounty> loadCacheCounties() {
        ArrayList<CacheCounty> arrayList = new ArrayList<CacheCounty>();
        Cursor cursor = db.query("CacheCounty", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                CacheCounty c = new CacheCounty();
                //TODO
                saveStateBean(c, cursor);
//                c.setId(cursor.getInt(cursor.getColumnIndex("id")));
//                c.setNum(cursor.getInt(cursor.getColumnIndex("num")));
//                c.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
//                c.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                arrayList.add(c);

            } while (cursor.moveToNext());
        }
        return arrayList;
    }


    public void saveStateBean(StateBean c, Cursor cursor) {
        Class cls = c.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            String columnName = "";
            if (column != null) {
                columnName = column.name();
            }
            field.setAccessible(true);
            String value = cursor.getString(cursor.getColumnIndex(columnName));
            try {
                Class type = field.getType();
                if (type.equals(BigDecimal.class)) {
                    field.set(c, new BigDecimal(value));
                } else if (type.equals(int.class)) {
                    field.set(c, Integer.valueOf(value));
                } else {
                    field.set(c, value);
                }
            } catch (IllegalAccessException e) {
                Log.v("EXCEPTION", e.getMessage());
            }
        }
    }

    public void saveProvince(Province p) {
        if (p != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", p.getProvinceName());
            values.put("province_code", p.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    public void saveCity(City c) {
        if (c != null) {
            ContentValues values = new ContentValues();
            values.put("city_code", c.getCityCode());
            values.put("city_name", c.getCityName());
            values.put("province_id", c.getProvinceId());
            db.insert("City", null, values);
        }
    }

    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("city_id", county.getCityId());
            values.put("county_code", county.getCountyCode());
            values.put("county_name", county.getCountyName());
            db.insert("County", null, values);
        }
    }


    public void saveCacheCounty(CacheCounty cacheCounty) {
        if (cacheCounty != null) {
            ContentValues values = new ContentValues();
            values.put("county_code", cacheCounty.getCountyCode());
            values.put("county_name", cacheCounty.getCountyName());
            values.put("num", cacheCounty.getNum());
            values.put("weather_code", cacheCounty.getWeatherCode());
            db.insert("CacheCounty", null, values);
        }
    }

    public void saveOfflineCounty(OfflineCounty offlineCounty) {
        if (offlineCounty != null) {
            ContentValues values = new ContentValues();
            values.put("city_id", offlineCounty.getCityId());
            values.put("county_code", offlineCounty.getCountyCode());
            values.put("weather_info", offlineCounty.getWeatherInfo().toString());
            values.put("last_update_time", offlineCounty.getLastUpdateTime());
            db.insert("OfflineCounty", null, values);
        }
    }

    public int getMaxNumCacheCounty() {
        Cursor cursor = db.query("CacheCounty", null, null, null, null, null, null);//TODO
        if (cursor.moveToLast()) {
            return cursor.getInt(cursor.getColumnIndex("num"));
        }
        return 0;
    }

    public boolean isExists(String countyCode) {
        Cursor cursor = db.query("CacheCounty", null, "county_code=?", new String[]{countyCode}, null, null, null);
        return cursor.moveToFirst();
    }

    public int getCountyNum(String countyCode) {
        Cursor cursor = db.query("CacheCounty", null, "county_code=?", new String[]{countyCode}, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("num"));
        }
        return 1;
    }

    public HashMap<String, String> getNextCache(int num) {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        Cursor cursor = db.query("CacheCounty", null, "num=?", new String[]{String.valueOf(num)}, null, null, null);
        if (cursor.moveToFirst()) {
            hashMap.put("code", cursor.getString(cursor.getColumnIndex("county_code")));
            hashMap.put("name", cursor.getString(cursor.getColumnIndex("county_name")));
            return hashMap;
        }
        return hashMap;
    }
}
