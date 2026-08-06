package vn.ots.qc.mobile.screen;

import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.interaction.ElementActions;
import vn.ots.qc.mobile.interaction.Gestures;
import io.appium.java_client.AppiumDriver;

public abstract class BaseScreen {
    protected final AppiumDriver driver;
    protected final FrameworkConfig config;
    protected final ElementActions actions;
    protected final Gestures gestures;

    protected BaseScreen(AppiumDriver driver, FrameworkConfig config) {
        this.driver = driver;
        this.config = config;
        this.actions = new ElementActions(driver, config);
        this.gestures = new Gestures(driver);
    }
}
