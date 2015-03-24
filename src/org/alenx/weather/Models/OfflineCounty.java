package org.alenx.weather.Models;

import org.alenx.weather.Utils.Annos.Column;
import org.alenx.weather.Utils.Annos.Table;
import org.json.JSONObject;

/*离线城市天气信息支持，再无法上网等其他情况下，读取本地城市信息进行展示*/
@Table(name = "offline_county",keyField = "id")
public class OfflineCounty extends StateBean {
    @Column(name = "id")
    private int id;
    @Column(name = "city_id")
    private String cityId;
    @Column(name = "weather_info")
    private JSONObject weatherInfo;
    @Column(name = "last_update_time")
    private String lastUpdateTime;
    @Column(name = "county_code")
    private String countyCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public JSONObject getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(JSONObject weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }
}
