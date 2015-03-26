package org.alenx.weather.DBUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*Models建表部分*/
public class WeatherDBOpenHelp extends SQLiteOpenHelper {

    /**
     *  Province表建表语句
     */
    public static final String CREATE_PROVINCE = "create table Province ("
            + "id integer primary key autoincrement, "
            + "province_name text, "
            + "province_code text)";
    /**
     *  City表建表语句
     */
    public static final String CREATE_CITY = "create table City ("
            + "id integer primary key autoincrement, "
            + "city_name text, "
            + "city_code text, "
            + "province_id integer)";
    /**
     *  County表建表语句
     */
    public static final String CREATE_COUNTY = "create table County ("
            + "id integer primary key autoincrement, "
            + "county_name text, "
            + "county_code text, "
            + "city_id integer)";


    /*CacheCounty建表sql*/
    public static final String CREATE_CACHE_COUNTY ="create table CacheCounty ("
            + "id integer primary key autoincrement, "
            + "num integer,"
            + "county_code text, "
            + "county_name text, "
            + "weather_code text)";


    /*OfflineCounty表建表sql*/
    public static final String CREATE_OFFLINE_COUNTY ="create table OfflineCounty ("
            + "id integer primary key autoincrement, "
            + "city_id text,"
            + "county_code text, "
            + "last_update_time text, "
            + "weather_info text)";


    public WeatherDBOpenHelp(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /*public WeatherDBOpenHelp(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTY);

        db.execSQL(CREATE_CACHE_COUNTY);
        db.execSQL(CREATE_OFFLINE_COUNTY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
