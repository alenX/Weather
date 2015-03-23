package org.alenx.weather.Models;

import org.alenx.weather.Utils.Annos.Column;
import org.alenx.weather.Utils.Annos.Table;

/*城市收藏夹*/
@Table(name = "CacheCounty", keyField = "id")
public class CacheCounty extends StateBean {

    @Column(name = "id")
    private int id;//内码
    @Column(name = "num")
    private int num;//编号从0开始，设置上限，最多存放的县个数
    @Column(name = "county_code")
    private String countyCode;//县编码
    @Column(name = "county_name")
    private String countyName;
    @Column(name = "weather_code")
    private String weatherCode;//天气编码

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }
}
