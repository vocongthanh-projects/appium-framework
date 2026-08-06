package vn.ots.qc.mobile.config;

public final class FrameworkConfigValidator {
    private FrameworkConfigValidator() { }

    public static void validate(FrameworkConfig config) {
        config.serverUri();
        config.appId();
        positive(config.explicitWait().toMillis(), "timeout.explicit.seconds");
        positive(config.pollingInterval().toMillis(), "timeout.polling.millis");
        positive(config.duration("timeout.newCommand.seconds").toMillis(), "timeout.newCommand.seconds");
        if (config.getBoolean("session.noReset") && config.getBoolean("session.fullReset")) {
            throw new ConfigurationException("session.noReset and session.fullReset cannot both be true");
        }
        port(config.platform() == Platform.ANDROID
                ? config.getInt("android.systemPort") : config.getInt("ios.wdaLocalPort"));
    }

    private static void positive(long value, String key) {
        if (value <= 0) throw new ConfigurationException("Setting " + key + " must be greater than zero");
    }

    private static void port(int value) {
        if (value < 1 || value > 65_535) throw new ConfigurationException("Appium forwarding port is invalid");
    }
}
