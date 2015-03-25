package org.alenx.weather.Utils;
public interface IHttpRequestListener {
    public void onExecute(String response);
    public void onError(Exception e,String path);
}