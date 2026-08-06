package vn.ots.qc.mobile.driver;

import org.testng.annotations.Test;
import vn.ots.qc.mobile.config.FrameworkConfig;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public final class DriverFactoryOptionsTest {
    @Test public void buildsAndroidCapabilitiesFromConfiguration() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "android.app.package", "com.example.application",
                "device.name", "contract-device",
                "android.systemPort", "8291"));
        var options = new DriverFactory().androidOptions(config);
        assertEquals(options.getCapability("appium:appPackage"), "com.example.application");
        assertEquals(options.getCapability("appium:systemPort"), 8291);
    }
}
