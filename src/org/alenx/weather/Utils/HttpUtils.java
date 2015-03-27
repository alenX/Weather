package org.alenx.weather.Utils;

import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
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
                        listener.onError(e, path);
                    }
                }
            }
        }).start();
    }

    /*离线图片下载*/
    public static void downWeatherPicture(String picPath, String path, String filename) {
        try {
            URL url = new URL(picPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();

            byte buffer[] = new byte[4 * 1024];
            File fileDir = new File(Environment.getExternalStorageDirectory() + "/" + path);
            fileDir.mkdir();

            File file = new File(Environment.getExternalStorageDirectory() + "/" + path + "/" + filename);
            if (file.exists()) {
                return;
            }
            Log.v("FILE", file.getAbsolutePath());
            file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);
            while ((inputStream.read(buffer)) != -1) {
                outputStream.write(buffer);
            }
            outputStream.flush();
        } catch (Exception e) {

        }
    }
}
