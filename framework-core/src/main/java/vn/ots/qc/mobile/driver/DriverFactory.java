package vn.ots.qc.mobile.driver;

import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.config.Platform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

public final class DriverFactory {
    public AppiumDriver create(FrameworkConfig config) {
        URL serverUrl;
        try {
            serverUrl = config.serverUri().toURL();
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid Appium server URL: " + config.serverUri(), exception);
        }

        return config.platform() == Platform.ANDROID
                ? new AndroidDriver(serverUrl, androidOptions(config))
                : new IOSDriver(serverUrl, iosOptions(config));
    }

    UiAutomator2Options androidOptions(FrameworkConfig config) {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(valueOr(config.get("device.name"), "Android device"))
                .setAppPackage(config.required("android.app.package"))
                .setNoReset(config.getBoolean("session.noReset"))
                .setFullReset(config.getBoolean("session.fullReset"))
                .setAutoGrantPermissions(config.getBoolean("android.autoGrantPermissions"))
                .setNewCommandTimeout(config.duration("timeout.newCommand.seconds"))
                .setSystemPort(config.getInt("android.systemPort"));
        optional(options, "appium:udid", config.get("device.udid"));
        optional(options, "appium:app", config.get("app.path"));
        applyCustomCapabilities(options, config.customCapabilities());
        return options;
    }

    XCUITestOptions iosOptions(FrameworkConfig config) {
        XCUITestOptions options = new XCUITestOptions()
                .setDeviceName(valueOr(config.get("device.name"), "iOS device"))
                .setBundleId(config.required("ios.app.bundleId"))
                .setNoReset(config.getBoolean("session.noReset"))
                .setFullReset(config.getBoolean("session.fullReset"))
                .setNewCommandTimeout(config.duration("timeout.newCommand.seconds"))
                .setWdaLocalPort(config.getInt("ios.wdaLocalPort"))
                .setWdaConnectionTimeout(Duration.ofSeconds(config.getInt("ios.wdaConnectionTimeout.seconds")));
        optional(options, "appium:udid", config.get("device.udid"));
        optional(options, "appium:platformVersion", config.get("device.platformVersion"));
        optional(options, "appium:app", config.get("app.path"));
        optional(options, "appium:xcodeOrgId", config.get("ios.xcodeOrgId"));
        optional(options, "appium:xcodeSigningId", config.get("ios.xcodeSigningId"));
        applyCustomCapabilities(options, config.customCapabilities());
        return options;
    }

    private static void applyCustomCapabilities(org.openqa.selenium.MutableCapabilities options, Map<String, String> capabilities) {
        capabilities.forEach((name, value) -> options.setCapability(normalizeCapabilityName(name), parseValue(value)));
    }

    private static void optional(org.openqa.selenium.MutableCapabilities options, String name, String value) {
        if (value != null && !value.isBlank()) {
            options.setCapability(name, value);
        }
    }

    private static String normalizeCapabilityName(String name) {
        return name.contains(":") ? name : "appium:" + name;
    }

    private static Object parseValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private static String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
