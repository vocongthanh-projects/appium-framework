package vn.ots.qc.mobile.cucumber;

import io.appium.java_client.AppiumDriver;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.context.MobileTestContext;
import vn.ots.qc.mobile.interaction.ElementActions;
import vn.ots.qc.mobile.interaction.Gestures;
import vn.ots.qc.mobile.interaction.MobileApplication;

public final class MobileWorld {
    private final MobileTestContext mobile = new MobileTestContext();
    public MobileTestContext mobile() { return mobile; }
    public AppiumDriver driver() { return mobile.driver(); }
    public FrameworkConfig config() { return mobile.config(); }
    public ElementActions actions() { return mobile.actions(); }
    public Gestures gestures() { return mobile.gestures(); }
    public MobileApplication application() { return mobile.application(); }
}
