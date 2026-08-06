package vn.ots.qc.mobile.context;

import io.appium.java_client.AppiumDriver;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.driver.DriverSession;
import vn.ots.qc.mobile.evidence.MobileEvidence;
import vn.ots.qc.mobile.evidence.MobileEvidenceCollector;
import vn.ots.qc.mobile.interaction.ElementActions;
import vn.ots.qc.mobile.interaction.Gestures;
import vn.ots.qc.mobile.interaction.MobileApplication;
import vn.ots.qc.mobile.device.ExecutionDevice;

public final class MobileTestContext implements AutoCloseable {
    private final FrameworkConfig config;
    private final DriverSession session;
    private ElementActions actions;
    private Gestures gestures;
    private MobileApplication application;

    public MobileTestContext() {
        this(ExecutionDevice.current().map(target -> target.applyTo(FrameworkConfig.load()))
                .orElseGet(FrameworkConfig::load));
    }
    public MobileTestContext(FrameworkConfig config) {
        this.config = config;
        session = new DriverSession(config);
    }

    public AppiumDriver start() {
        AppiumDriver driver = session.start();
        actions = new ElementActions(driver, config);
        gestures = new Gestures(driver);
        application = new MobileApplication(driver, config);
        return driver;
    }

    public MobileEvidence finish(String name, boolean failed) {
        try {
            return failed ? new MobileEvidenceCollector(config).capture(driver(), name) : new MobileEvidence(null, null);
        } finally { close(); }
    }

    public AppiumDriver driver() { return session.driver(); }
    public FrameworkConfig config() { return config; }
    public ElementActions actions() { return java.util.Objects.requireNonNull(actions, "Context has not started"); }
    public Gestures gestures() { return java.util.Objects.requireNonNull(gestures, "Context has not started"); }
    public MobileApplication application() { return java.util.Objects.requireNonNull(application, "Context has not started"); }
    @Override public void close() { session.close(); actions = null; gestures = null; application = null; }
}
