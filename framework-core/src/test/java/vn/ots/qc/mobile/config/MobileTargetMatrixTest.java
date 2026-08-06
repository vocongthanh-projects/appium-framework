package vn.ots.qc.mobile.config;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.ots.qc.mobile.device.DeviceTarget;
import vn.ots.qc.mobile.device.MobileTargetMatrix;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MobileTargetMatrixTest {
    @Test
    public void buildsOrderedIsolatedTargets() {
        Properties values = base();
        values.setProperty("device.targets", "Pixel QA, iPhone QA");
        values.setProperty("device.pixel-qa.platform", "android");
        values.setProperty("device.pixel-qa.udid", "emulator-5554");
        values.setProperty("device.pixel-qa.system-port", "8201");
        values.setProperty("device.iphone-qa.platform", "ios");
        values.setProperty("device.iphone-qa.udid", "ios-001");
        values.setProperty("device.iphone-qa.wda-local-port", "8101");
        values.setProperty("device.iphone-qa.bundle-id", "com.example.ios");

        List<DeviceTarget> targets = MobileTargetMatrix.from(FrameworkConfig.load(values, Map.of()));

        Assert.assertEquals(targets.stream().map(DeviceTarget::id).toList(), List.of("pixel-qa", "iphone-qa"));
        Assert.assertEquals(targets.get(0).overrides().get("android.systemPort"), "8201");
        Assert.assertEquals(targets.get(1).overrides().get("ios.app.bundleId"), "com.example.ios");
    }

    @Test
    public void rejectsPortCollisionBeforeSessionsStart() {
        Properties values = base();
        values.setProperty("device.targets", "one,two");
        values.setProperty("device.one.udid", "emulator-1");
        values.setProperty("device.two.udid", "emulator-2");
        values.setProperty("device.one.system-port", "8201");
        values.setProperty("device.two.system-port", "8201");

        Assert.expectThrows(ConfigurationException.class,
                () -> MobileTargetMatrix.from(FrameworkConfig.load(values, Map.of())));
    }

    private static Properties base() {
        Properties values = new Properties();
        values.setProperty("platform", "android");
        values.setProperty("android.app.package", "com.example.android");
        values.setProperty("android.systemPort", "8200");
        values.setProperty("ios.wdaLocalPort", "8100");
        return values;
    }
}
