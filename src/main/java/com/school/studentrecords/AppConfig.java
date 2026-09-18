package com.school.studentrecords;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class AppConfig {
    private final Properties values = new Properties();
    AppConfig() {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input == null) throw new IllegalStateException("application.properties is missing");
            values.load(input);
        } catch (IOException e) { throw new IllegalStateException("Cannot read configuration", e); }
    }
    String get(String key) { return values.getProperty(key); }
}

