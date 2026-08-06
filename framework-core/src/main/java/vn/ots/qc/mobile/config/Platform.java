package vn.ots.qc.mobile.config;

import java.util.Locale;

public enum Platform {
    ANDROID,
    IOS;

    public static Platform from(String value) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Missing required setting: platform");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ConfigurationException("Unsupported platform '" + value + "'. Use android or ios.", exception);
        }
    }
}
