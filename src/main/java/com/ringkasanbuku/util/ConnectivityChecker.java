package com.ringkasanbuku.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectivityChecker {

    public boolean hasInternet() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://www.google.com").openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (IOException e) {
            return false;
        }
    }
}
