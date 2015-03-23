package org.alenx.weather.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import org.alenx.weather.Receiver.WeatherAutoUpdateRec;
import org.alenx.weather.Utils.HttpRequestListener;
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
        //TODO
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour = 8 * 60 * 60 * 1000;
        long time = SystemClock.elapsedRealtime() + hour;
        Intent i = new Intent(this, WeatherAutoUpdateRec.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pi);
        return super.onStartCommand(i, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String weatherCode = sp.getString("weather_code", "");

        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";

        HttpUtils.sendHttpRequest(address, new HttpRequestListener() {
            @Override
            public void onExecute(String response) {
                Log.d("TAG", response);
                Utils.handleWeatherResponse(WeatherAutoUpdateSer.this, response);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
