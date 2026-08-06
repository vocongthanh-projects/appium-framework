package vn.ots.qc.mobile.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Immutable configuration snapshot created once for each scenario. */
public final class FrameworkConfig {
    private static final String DEFAULT_CONFIG = "/config/framework.properties";
    private static final String APPLICATION_CONFIG = "/config/application.properties";
    private static final String CONFIG_FILE_PROPERTY = "config.file";
    private static final String CONFIG_FILE_ENV = "CONFIG_FILE";

    private final Properties values;
    private final Map<String, String> environment;
    private final Properties systemProperties;

    private FrameworkConfig(Properties values, Map<String, String> environment, Properties systemProperties) {
        this.values = values;
        this.environment = environment;
        this.systemProperties = systemProperties;
    }

    public static FrameworkConfig load() {
        return load(System.getProperties(), System.getenv());
    }

    public FrameworkConfig withOverrides(Map<String, String> overrides) {
        Properties overridden = new Properties();
        overridden.putAll(systemProperties);
        overrides.forEach((key, value) -> {
            if (value != null) overridden.setProperty(key, value);
        });
        Properties snapshot = new Properties();
        snapshot.putAll(values);
        return new FrameworkConfig(snapshot, environment, overridden);
    }

    static FrameworkConfig load(Properties systemProperties, Map<String, String> environment) {
        Properties merged = new Properties();
        try (InputStream defaults = FrameworkConfig.class.getResourceAsStream(DEFAULT_CONFIG)) {
            if (defaults == null) {
                throw new ConfigurationException("Classpath resource not found: " + DEFAULT_CONFIG);
            }
            merged.load(defaults);
        } catch (IOException exception) {
            throw new ConfigurationException("Cannot load " + DEFAULT_CONFIG, exception);
        }

        try (InputStream application = FrameworkConfig.class.getResourceAsStream(APPLICATION_CONFIG)) {
            if (application != null) merged.load(application);
        } catch (IOException exception) {
            throw new ConfigurationException("Cannot load " + APPLICATION_CONFIG, exception);
        }

        String externalPath = firstNonBlank(
                systemProperties.getProperty(CONFIG_FILE_PROPERTY),
                environment.get(CONFIG_FILE_ENV)
        );
        if (externalPath != null) {
            Path path = Path.of(externalPath).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                throw new ConfigurationException("Config file does not exist: " + path);
            }
            try (InputStream override = Files.newInputStream(path)) {
                merged.load(override);
            } catch (IOException exception) {
                throw new ConfigurationException("Cannot load config file: " + path, exception);
            }
        }

        Properties systemSnapshot = new Properties();
        systemSnapshot.putAll(systemProperties);
        return new FrameworkConfig(merged, Map.copyOf(environment), systemSnapshot);
    }

    public String get(String key) {
        String systemValue = systemProperties.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String environmentValue = environment.get(toEnvironmentKey(key));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        return values.getProperty(key, "").trim();
    }

    public String required(String key) {
        String value = get(key);
        if (value.isBlank()) {
            throw new ConfigurationException("Missing required setting: " + key);
        }
        return value;
    }

    public boolean getBoolean(String key) {
        String value = required(key).toLowerCase(Locale.ROOT);
        if (!value.equals("true") && !value.equals("false")) {
            throw new ConfigurationException("Setting " + key + " must be true or false, but was: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value.isBlank()) return defaultValue;
        return parseBoolean(key, value);
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(required(key));
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be an integer", exception);
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value.isBlank()) return defaultValue;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be an integer", exception);
        }
    }

    private boolean parseBoolean(String key, String raw) {
        String value = raw.toLowerCase(Locale.ROOT);
        if (!value.equals("true") && !value.equals("false")) {
            throw new ConfigurationException("Setting " + key + " must be true or false, but was: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public Duration duration(String key) {
        int seconds = getInt(key);
        if (seconds < 0) {
            throw new ConfigurationException("Setting " + key + " cannot be negative");
        }
        return Duration.ofSeconds(seconds);
    }

    public Platform platform() {
        return Platform.from(required("platform"));
    }

    public URI serverUri() {
        try {
            return URI.create(required("appium.server.url"));
        } catch (IllegalArgumentException exception) {
            throw new ConfigurationException("Invalid appium.server.url", exception);
        }
    }

    public String appId() {
        return platform() == Platform.ANDROID
                ? required("android.app.package")
                : required("ios.app.bundleId");
    }

    public Duration explicitWait() {
        return duration("timeout.explicit.seconds");
    }

    public Duration pollingInterval() {
        return Duration.ofMillis(getInt("timeout.polling.millis"));
    }

    public Map<String, String> customCapabilities() {
        Properties snapshot = new Properties();
        snapshot.putAll(values);
        environment.forEach((key, value) -> {
            if (key.startsWith("CAPABILITY_")) {
                snapshot.setProperty("capability." + key.substring("CAPABILITY_".length()).toLowerCase(Locale.ROOT), value);
            }
        });
        systemProperties.stringPropertyNames().stream()
                .filter(key -> key.startsWith("capability."))
                .forEach(key -> snapshot.setProperty(key, systemProperties.getProperty(key)));
        return snapshot.stringPropertyNames().stream()
                .filter(key -> key.startsWith("capability."))
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        key -> key.substring("capability.".length()),
                        key -> snapshot.getProperty(key).trim()
                ));
    }

    private static String toEnvironmentKey(String key) {
        return key.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[^A-Za-z0-9]", "_")
                .toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
