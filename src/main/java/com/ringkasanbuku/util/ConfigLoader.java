package com.ringkasanbuku.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try {
            InputStream input =
                    ConfigLoader.class
                            .getClassLoader()
                            .getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException("config.properties tidak ditemukan");
            }

            props.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Gagal membaca config.properties", e);
        }
    }

    public static String getOpenRouterApiKey() {
    return props.getProperty("openrouter.api.key");
}
}