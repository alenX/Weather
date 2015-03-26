package org.alenx.weather.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.alenx.weather.DBUtils.WeatherDBHelp;
import org.alenx.weather.Models.CacheCounty;
import org.alenx.weather.Models.OfflineCounty;
import org.alenx.weather.R;
import org.alenx.weather.Services.WeatherAutoUpdateSer;
import org.alenx.weather.Utils.HttpUtils;
import org.alenx.weather.Utils.IHttpRequestListener;
import org.alenx.weather.Utils.Utils;

import java.util.ArrayList;

public class WeatherShowAct extends Activity implements View.OnClickListener {

    LinearLayout weatherLayout;
    TextView mCityNameText;
    TextView mPublishText;
    TextView mWeatherDespText;
    TextView mTemp1Text;
    TextView mTemp2Text;
    TextView mCurrentDateText;
    Button mSwitchButton;
    Button mRefreshButton;
    String county_name_title;

    RelativeLayout layout;

    WeatherDBHelp dbHelp;

    /*收藏夹城市*/
    ArrayList<CacheCounty> aCacheCounties;

    ProgressDialog pd;


    String countyCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weathershow);

        weatherLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        mCityNameText = (TextView) findViewById(R.id.city_name);
        mPublishText = (TextView) findViewById(R.id.publish_text);
        mWeatherDespText = (TextView) findViewById(R.id.weather_desp);
        mTemp1Text = (TextView) findViewById(R.id.temp1);
        mTemp2Text = (TextView) findViewById(R.id.temp2);
        mCurrentDateText = (TextView) findViewById(R.id.current_date);

        mSwitchButton = (Button) findViewById(R.id.switch_city);
        mRefreshButton = (Button) findViewById(R.id.refresh_weather);

        layout = (RelativeLayout) findViewById(R.id.detail_info);


        dbHelp = WeatherDBHelp.getInstance(this);
        aCacheCounties = dbHelp.loadCacheCounties();//加载收藏夹城市信息
        countyCode = getIntent().getStringExtra("county_code");//获取传递的参数
        county_name_title = getIntent().getStringExtra("county_name_title");//获取传递的参数
        Log.v("WeatherShowAct", countyCode);
        Log.v("WeatherShowAct", county_name_title);

        if (!TextUtils.isEmpty(countyCode)) {
            mPublishText.setText("正在同步...");
            weatherLayout.setVisibility(View.INVISIBLE);
            mCityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);

        } else {
            showWeather();
        }

        mSwitchButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);

        layout.setOnTouchListener(new WeatherGesture());
    }


    class WeatherGesture implements GestureDetector.OnGestureListener, View.OnTouchListener {

        GestureDetector detector = new GestureDetector(getApplicationContext(), this);

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            int dx = (int) (e2.getX() - e1.getX());
            CacheCounty cacheCounty = new CacheCounty();
            cacheCounty.setCountyCode(getIntent().getStringExtra("county_code"));


            /*int max_num = dbHelp.getMaxNumCacheCounty();
            int current_num = dbHelp.getCountyNum(cacheCounty.getCountyCode());
            int num=0;*/
            int current_num = -1;
            for (int i = 0; i < aCacheCounties.size(); i++) {
                if (aCacheCounties.get(i).getCountyCode().equals(cacheCounty.getCountyCode())) {
                    current_num = i;
                    break;
                }
            }
            if (current_num == -1) {
                return false;
            }

            int max_num = aCacheCounties.size();

            int num = 0;

            if (Math.abs(dx) > 10 && dx > 0) {
                Log.v("aa", "→");//向前翻
                num = (current_num - 1) % max_num;
            }
            if (Math.abs(dx) > 10 && dx < 0) {
                Log.v("aa", "←");//向后翻
                num = (current_num + 1) % max_num;
            }
            //HashMap<String,String> hashMap = dbHelp.getNextCache(num);
            /*cacheCounty.setCountyName(aCacheCounties.get(num).getCountyName());
            cacheCounty.setCountyCode(aCacheCounties.get(num).getCountyCode());
            Intent intent = new Intent(getApplicationContext(), WeatherShowAct.class);
            intent.putExtra("county_code", cacheCounty.getCountyCode());
            intent.putExtra("county_name_title", cacheCounty.getCountyName());
            startActivity(intent);*/

            pd = new ProgressDialog(WeatherShowAct.this);
            pd.setMessage("正在加载中...");
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            mPublishText.setText("正在同步...");
            mCityNameText.setText("正在同步...");
            queryWeatherCode(aCacheCounties.get(num).getCountyCode());
            return true;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return detector.onTouchEvent(event);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseCityAct.class);
                intent.putExtra("from_weather_act", true);

                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                mPublishText.setText("同步中...");
                SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = spf.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;

        }
    }


    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }


    public void queryFromServer(String address, final String type) {
        HttpUtils.sendHttpRequest(address, new IHttpRequestListener() {
            @Override
            public void onExecute(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {//TODO
                        String[] str = response.split("\\|");
                        if (str.length > 0) {
                            String weatherCode = str[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utils.handleWeatherResponse(WeatherShowAct.this, response, dbHelp, countyCode);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();

                        }
                    });
                }
            }

            @Override
            public void onError(Exception e, String path) {
//                boolean isQuery = iep.queryFromOffline();
                /* if (!isQuery) {

                }*/
                //http://www.weather.com.cn/data/cityinfo/101300103.html
                String weatherCode = path.contains("cityinfo") ? path.split("\\.")[3].split("\\/")[4] : "";
                if (TextUtils.isEmpty(weatherCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPublishText.setText("同步失败");
                        }
                    });
                } else {
                    OfflineCounty offlineCounty = dbHelp.getOfflineCounty(countyCode);
                    if (offlineCounty == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPublishText.setText("同步失败");
                            }
                        });
                    } else {
                        Utils.handleWeatherOffline(WeatherShowAct.this, dbHelp.getOfflineCounty(countyCode).getWeatherInfo().toString(), dbHelp, countyCode);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();

                            }
                        });
                    }

                }
            }
        });
    }

    public void showWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WeatherShowAct.this);
        mCityNameText.setText(sp.getString("city_name", ""));
//        mCityNameText.setText(county_name_title);
        mTemp1Text.setText(sp.getString("temp1", ""));
        mTemp2Text.setText(sp.getString("temp2", ""));
        mWeatherDespText.setText(sp.getString("weatherDesp", ""));
        mPublishText.setText("今天" + sp.getString("publish_time", "") + "发布");
        mCurrentDateText.setText(sp.getString("current_date", ""));

        weatherLayout.setVisibility(View.VISIBLE);
        mCityNameText.setVisibility(View.VISIBLE);
        if (pd != null) {
            pd.dismiss();
        }
        Intent intent = new Intent(this, WeatherAutoUpdateSer.class);
        startService(intent);
    }
}
