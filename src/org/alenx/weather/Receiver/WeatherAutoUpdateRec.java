package org.alenx.weather.Receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.alenx.weather.Services.WeatherAutoUpdateSer;

public class WeatherAutoUpdateRec extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, WeatherAutoUpdateSer.class);
        context.startService(i);
    }
}
