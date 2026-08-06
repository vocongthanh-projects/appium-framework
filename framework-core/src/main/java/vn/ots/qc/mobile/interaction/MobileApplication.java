package vn.ots.qc.mobile.interaction;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;
import vn.ots.qc.mobile.config.FrameworkConfig;

import java.time.Duration;

/** Generic application lifecycle using the configured package or bundle identifier. */
public final class MobileApplication {
    private final InteractsWithApps apps;
    private final String appId;

    public MobileApplication(AppiumDriver driver, FrameworkConfig config) {
        if (!(driver instanceof InteractsWithApps applicationCommands)) {
            throw new IllegalArgumentException("Driver does not support application lifecycle commands");
        }
        this.apps = applicationCommands;
        this.appId = config.appId();
    }

    public void activate() { apps.activateApp(appId); }
    public boolean terminate() { return apps.terminateApp(appId); }
    public void restart() { terminate(); activate(); }
    public void background(Duration duration) {
        if (duration == null || duration.isNegative()) throw new IllegalArgumentException("duration cannot be negative");
        apps.runAppInBackground(duration);
    }
    public boolean isInstalled() { return apps.isAppInstalled(appId); }
    public ApplicationState state() { return apps.queryAppState(appId); }
}
