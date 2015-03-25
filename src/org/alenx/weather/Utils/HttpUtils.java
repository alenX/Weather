package org.alenx.weather.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtils {
    public static void sendHttpRequest(final String path, final IHttpRequestListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    URL url = new URL(path);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onExecute(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e,path);
                    }
                }
            }
        }).start();
    }
}
