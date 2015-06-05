package org.alenx.weather.Services;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import org.alenx.weather.Activities.WeatherShowAct;
import org.alenx.weather.DBUtils.WeatherDBHelp;
import org.alenx.weather.R;
import org.alenx.weather.Receiver.WeatherAutoUpdateRec;
import org.alenx.weather.Utils.IHttpRequestListener;
import org.alenx.weather.Utils.HttpUtils;
import org.alenx.weather.Utils.Utils;

public class WeatherAutoUpdateSer extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();


        Resources r = getResources();
        PendingIntent pi= PendingIntent.getActivity(this, 0, new Intent(this, WeatherShowAct.class), 0);

        Notification notification = new Notification.Builder(this)
                .setTicker(r.getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(r.getString(R.string.app_name))
                .setContentIntent(pi)
                .setAutoCancel(true).build();

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0,notification);



        //TODO
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour = 8 * 60 * 60 * 1000;
        long time = SystemClock.elapsedRealtime() + hour;
        Intent i = new Intent(this, WeatherAutoUpdateRec.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pi);
        return super.onStartCommand(i, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String weatherCode = sp.getString("weather_code", "");
        final String countyCode = sp.getString("county_code", "");//TODO

        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";

        HttpUtils.sendHttpRequest(address, new IHttpRequestListener() {
            @Override
            public void onExecute(String response) {
                Log.d("TAG", response);
                Utils.handleWeatherResponse(WeatherAutoUpdateSer.this, response, WeatherDBHelp.getInstance(getApplicationContext()),countyCode);
            }

            @Override
            public void onError(Exception e,String path) {

            }
        });
    }
}
