
package com.barraca.dao;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) props.load(in);
        } catch (Exception ignored) {}
    }

    private AppConfig() {}

    public static String get(String key, String def) {
        return props.getProperty(key, def);
    }

    public static boolean simulacao() {
        return Boolean.parseBoolean(get("app.simulacao", "false"));
    }
}
