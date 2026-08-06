package vn.ots.qc.mobile.driver;

import vn.ots.qc.mobile.config.FrameworkConfig;
import io.appium.java_client.AppiumDriver;

import java.util.Objects;

/** Scenario-scoped owner of one Appium session. */
public final class DriverSession implements AutoCloseable {
    private final FrameworkConfig config;
    private final DriverFactory factory;
    private AppiumDriver driver;

    public DriverSession() {
        this(FrameworkConfig.load(), new DriverFactory());
    }

    public DriverSession(FrameworkConfig config) {
        this(config, new DriverFactory());
    }

    DriverSession(FrameworkConfig config, DriverFactory factory) {
        this.config = Objects.requireNonNull(config);
        this.factory = Objects.requireNonNull(factory);
    }

    public synchronized AppiumDriver start() {
        if (driver == null) {
            vn.ots.qc.mobile.config.FrameworkConfigValidator.validate(config);
            driver = factory.create(config);
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        }
        return driver;
    }

    public AppiumDriver driver() {
        return Objects.requireNonNull(driver, "Driver has not been started for this scenario");
    }

    public FrameworkConfig config() {
        return config;
    }

    @Override
    public synchronized void close() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }
}
