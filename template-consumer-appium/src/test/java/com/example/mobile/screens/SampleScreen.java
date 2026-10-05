package com.example.mobile.screens;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.locator.LocatorSet;
import vn.ots.qc.mobile.screen.BaseScreen;

/** Example consumer screen. Replace the sample accessibility IDs with app-owned identifiers. */
public final class SampleScreen extends BaseScreen {
    private static final LocatorSet TITLE = LocatorSet.same(AppiumBy.accessibilityId("sample.screen.title"));

    public SampleScreen(AppiumDriver driver, FrameworkConfig config) {
        super(driver, config);
    }

    public boolean isTitleVisible() {
        return actions.isVisible(TITLE, config.explicitWait());
    }
}
