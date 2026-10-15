package vn.ots.qc.consumer.template.mobile.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.ots.qc.mobile.testng.BaseMobileTest;

/** Starts the configured application session; requires a running Appium server and available device. */
public final class MobileSmokeTest extends BaseMobileTest {
    @Test(groups = "smoke")
    public void startsAnApplicationSession() {
        Assert.assertNotNull(driver().getSessionId(), "Appium should create a session for the configured app");
    }
}
