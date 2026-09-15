package bases;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Enhanced ConfigManager - Robust loading for both default and local configurations.
 * Correctly prioritizes config-local.properties for account information.
 */
public class ConfigManager {

    private static final Properties props = new Properties();
    private static final ThreadLocal<String> threadUdid = new ThreadLocal<>();


    static {
        loadProperties();
    }

    private static void loadProperties() {
        String userDir = System.getProperty("user.dir");
        
        // 1. Load config.properties (Base)
        loadFromFile(new File(userDir, "src/test/resources/config.properties"), "/config.properties");
        
        // 2. Load config-local.properties (Override)
        loadFromFile(new File(userDir, "src/test/resources/config-local.properties"), "/config-local.properties");
        
        System.out.println("✅ ConfigManager: Initialized with base and local properties.");
    }

    private static void loadFromFile(File file, String resourcePath) {
        try (InputStream input = file.exists() ? new FileInputStream(file) : 
                ConfigManager.class.getResourceAsStream(resourcePath)) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException ignored) {}
    }

    private ConfigManager() {}

    public static String get(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();

        String envValue = System.getenv(key.replace(".", "_").toUpperCase());
        if (envValue != null && !envValue.isBlank()) return envValue.trim();

        return props.getProperty(key, "");
    }

    public static int getInt(String key) {
        try {
            String val = get(key);
            return (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val);
        } catch (Exception e) { return 0; }
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }


    public static String getUdid() {
        String threadVal = threadUdid.get();
        return (threadVal != null) ? threadVal : get("android.udid");
    }


    public static String getAppPackage() { return get("app.package"); }
    public static int getAppiumPort() { return getInt("appium.server.port"); }
    public static int getDefaultTimeout() { return getInt("timeout.default"); }
    public static String getScreenshotPath() { return get("report.screenshot.path"); }
    
    public static String getUsername() { return get("test.username"); }
    public static String getPassword() { return get("test.password"); }
    
    public static String getExcelReportPath(String profile) {
        return get("report.excel.path." + profile);
    }
}
