package org.alenx.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import org.alenx.weather.Activities.ChooseCityAct;

public class LoadingAct extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RedirectAct();
            }
        }, 3000);
    }


    public void RedirectAct() {
        Intent intent = new Intent(LoadingAct.this, ChooseCityAct.class);
        startActivity(intent);
        LoadingAct.this.finish();
    }
}
