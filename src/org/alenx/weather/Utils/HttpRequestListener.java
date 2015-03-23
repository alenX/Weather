package org.alenx.weather.Utils;
public interface HttpRequestListener {
    public void onExecute(String response);
    public void onError(Exception e);
}
