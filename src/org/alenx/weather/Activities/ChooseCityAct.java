package org.alenx.weather.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import org.alenx.weather.DBUtils.WeatherDBHelp;
import org.alenx.weather.Models.CacheCounty;
import org.alenx.weather.Models.City;
import org.alenx.weather.Models.County;
import org.alenx.weather.Models.Province;
import org.alenx.weather.R;
import org.alenx.weather.Utils.HttpUtils;
import org.alenx.weather.Utils.IHttpRequestListener;
import org.alenx.weather.Utils.Utils;
import org.xmlpull.v1.XmlPullParser;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class ChooseCityAct extends Activity {

    ListView listView;//数据展示列表
    ArrayAdapter<String> adapter;//数据列表的数据集和
    TextView title_text;//标题
    ArrayList<String> dataList = new ArrayList<String>();

    ArrayList<Province> arrPro = new ArrayList<Province>();//省份
    ArrayList<City> arrCity = new ArrayList<City>();//市
    ArrayList<County> arrCounty = new ArrayList<County>();//县

    WeatherDBHelp dbHelp;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    ProgressDialog pd;//加载信息展示


    Province selectedProvince;
    City selectedCity;
    int current_level;


    String title = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//TODO
        setContentView(R.layout.choosecity);
        getCitiesInfo();



        dbHelp = WeatherDBHelp.getInstance(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String cacheCountyCode = sharedPreferences.getString("cacheCountyCode", "");
        String cacheCountyName = sharedPreferences.getString("cacheCountyName", "");


        boolean isFromWeatherDetail = getIntent().getBooleanExtra("from_weather_act", false);


//        Socket socket = new Socket();


        if (!TextUtils.isEmpty(cacheCountyCode) && !isFromWeatherDetail) {
            String countyCode = cacheCountyCode;
            Intent intent = new Intent(ChooseCityAct.this, WeatherShowAct.class);
            intent.putExtra("county_code", countyCode);
            intent.putExtra("county_name_title", cacheCountyName);
            startActivity(intent);
            finish();
        } else {
            listView = (ListView) findViewById(R.id.list_view);
            title_text = (TextView) findViewById(R.id.title_text);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
            listView.setAdapter(adapter);
//            listView.setAdapter(new CityAdapter(dataList,this));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (current_level == LEVEL_PROVINCE) {
                        selectedProvince = arrPro.get(position);//根据所处的层级获取不同层级对应的对象
                        title += selectedProvince.getProvinceName();
                        showCity();
                    } else if (current_level == LEVEL_CITY) {
                        selectedCity = arrCity.get(position);
                        if (!title.equals(selectedCity.getCityName())) {
                            title += "-" + selectedCity.getCityName();
                        }

                        showCounty();
                    } else if (current_level == LEVEL_COUNTY) {
                        String countyCode = arrCounty.get(position).getCountyCode();
                        Intent intent = new Intent(ChooseCityAct.this, WeatherShowAct.class);
                        intent.putExtra("county_code", countyCode);
                        String county_name_title = "";
                        if (!title.equals(arrCounty.get(position).getCountyName())) {
                            county_name_title = title + "-" + arrCounty.get(position).getCountyName();
                        } else {
                            county_name_title = title;
                        }
                        intent.putExtra("county_name_title", county_name_title);
                        startActivity(intent);
                        finish();
                    }
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    if (current_level == LEVEL_COUNTY) {// 只有县时响应
                        //可以将县存放，保存多个地区供选择，避免每次进入时选择,增加单一收藏夹功能
                        //TODO 多个地区的支持
                        AlertDialog.Builder ab = new AlertDialog.Builder(ChooseCityAct.this);
                        ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final County county = arrCounty.get(position);
                                Log.v("DD", county.getCountyCode());
                                SharedPreferences.Editor spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                spf.putString("cacheCountyCode", county.getCountyCode());
                                spf.putString("cacheCountyName", county.getCountyName());
                                spf.commit();

                                //TODO 将县城收藏夹
                                String getWeatherCodePath = "http://www.weather.com.cn/data/list3/city" + county.getCountyCode() + ".xml";
                                HttpUtils.sendHttpRequest(getWeatherCodePath, new IHttpRequestListener() {
                                    @Override
                                    public void onExecute(String response) {
                                        if (!TextUtils.isEmpty(response)) {//TODO
                                            String[] str = response.split("\\|");
                                            if (str != null && str.length > 0) {
                                                String weatherCode = str[1];
                                                CacheCounty cacheCounty = new CacheCounty();
                                                ArrayList<CacheCounty> arr = dbHelp.loadCacheCounties();
                                                cacheCounty.setNum(arr.size() + 1);
                                                cacheCounty.setCountyCode(county.getCountyCode());
                                                cacheCounty.setCountyName(county.getCountyName());
                                                cacheCounty.setWeatherCode(weatherCode);
                                                if (dbHelp.isExists(cacheCounty.getCountyCode())) {
                                                    Toast.makeText(getApplicationContext(), "该城市已经收藏！", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (cacheCounty.getNum() >= 6) {
                                                        Toast.makeText(getApplicationContext(), "系统已经存在五个收藏的城市，无法继续收藏", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        dbHelp.saveCacheCounty(cacheCounty);
                                                        Toast.makeText(getApplicationContext(), "收藏成功！", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(Exception e, String path) {

                                    }
                                });


                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setTitle(R.string.isFavour).show();


                    }
                    return false;
                }
            });

            showProvince();
        }
    }

    /*加载省份数据，展示List*/
    public void showProvince() {
        arrPro = dbHelp.loadProvince();
        if (arrPro.size() < 1) {// 从服务器查询
            queryFromServer(null, "p");
        } else {//从本地SQLite查询
            dataList.clear();
            for (Province p : arrPro) {
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            title_text.setText(R.string.china);
            current_level = LEVEL_PROVINCE;
        }
    }

    /*加载市信息*/
    public void showCity() {
        arrCity = dbHelp.loadCity(selectedProvince.getId());
        if (arrCity.size() < 1) {// 从服务器查询
            queryFromServer(selectedProvince.getProvinceCode(), "c");
        } else {//从本地SQLite查询
            dataList.clear();
            for (City c : arrCity) {
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            title_text.setText(selectedProvince.getProvinceName());
            current_level = LEVEL_CITY;
        }
    }

    /*加载县信息*/
    public void showCounty() {
        arrCounty = dbHelp.loadCounty(selectedCity.getId());
        if (arrCounty.size() < 1) {// 从服务器查询
            queryFromServer(selectedCity.getCityCode(), "cc");
        } else {//从本地SQLite查询
            dataList.clear();
            for (County c : arrCounty) {
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            title_text.setText(selectedCity.getCityName());
            current_level = LEVEL_COUNTY;
        }
    }

    /*从数据库查询天气信息*/
    public void queryFromServer(String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtils.sendHttpRequest(address, new IHttpRequestListener() {
            @Override
            public void onExecute(String response) {
                boolean result = false;
                if ("p".equals(type)) {
                    result = Utils.handleProvince(dbHelp, response);
                } else if ("c".equals(type)) {
                    result = Utils.handleCity(dbHelp, response, selectedProvince.getId());
                } else if ("cc".equals(type)) {
                    result = Utils.handleCounty(dbHelp, response, selectedCity.getId());
                }

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("p".equals(type)) {
                                showProvince();
                            } else if ("c".equals(type)) {
                                showCity();
                            } else if ("cc".equals(type)) {
                                showCounty();
                            }

                        }
                    });
                }
            }

            @Override
            public void onError(Exception e, String path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseCityAct.this, R.string.failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    public void showProgressDialog() {
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setMessage(R.string.loading + "");
            pd.setCanceledOnTouchOutside(false);
        }
        pd.show();
    }

    public void closeProgressDialog() {
        if (pd != null) {
            pd.dismiss();
        }
    }


    public void getCitiesInfo(){
        XmlResourceParser xrp = getResources().getXml(R.xml.chinacities);

        try {
          while (xrp.getEventType()!= XmlPullParser.END_DOCUMENT){
              HashMap<String,List<HashMap<String,List>>> hashMap = new HashMap<String, List<HashMap<String, List>>>();
              List<HashMap<String,List>> list = new ArrayList<HashMap<String, List>>();
              HashMap<String,List> map = new HashMap<String, List>();
              List codeList = new ArrayList();
              if (xrp.getEventType()==XmlPullParser.START_TAG){
                  String tagName = xrp.getName();

                  String pName = "";
                  String cName = "";
                  if (tagName.equals("province")){
                      Log.d("aa",xrp.getAttributeValue(null,"id"));
                      pName = xrp.getAttributeValue(null,"name");
                  }else if(tagName.equals("city")){
                      Log.d("bb",xrp.getAttributeValue(null,"id"));
                      cName= xrp.getAttributeValue(null,"name");
                  }else if(tagName.equals("county")){
                      Log.d("cc",xrp.getAttributeValue(null,"id"));
                      Log.d("cc",xrp.getAttributeValue(null,"weatherCode"));
                      codeList.add(xrp.getAttributeValue(null,"name"));
                  }

              }
              xrp.next();


          }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class CityAdapter extends BaseAdapter{

        private ArrayList<String> cities=null;
        private Context context=null;


        public CityAdapter(ArrayList<String> cities, Context context) {
            this.cities = cities;
            this.context = context;
        }

        @Override
        public int getCount() {
            return cities.size();
        }

        @Override
        public Object getItem(int position) {
            return cities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder item = null;

            if (convertView==null){
                convertView = LayoutInflater.from(context).inflate(R.layout.city_list_item,null);

                item = new ViewHolder();
                item.city_name_item=(TextView)convertView.findViewById(R.id.city_name_item);
                item.city_name_tips=(TextView)convertView.findViewById(R.id.city_name_tips);
//                item.city_name_tips = ""
                convertView.setTag(item);
            }else{
                item = (ViewHolder)convertView.getTag();
            }

            item.city_name_tips.setText("城市");
            item.city_name_item.setText(cities.get(position));
            return convertView;
        }
    }

    class ViewHolder{
        public TextView city_name_item;
        public TextView city_name_tips ;

    }
}

