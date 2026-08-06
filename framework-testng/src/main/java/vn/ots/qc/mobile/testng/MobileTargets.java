package vn.ots.qc.mobile.testng;

import org.testng.annotations.DataProvider;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.device.MobileTargetMatrix;

/** TestNG provider for consumers that explicitly opt into isolated multi-device execution. */
public final class MobileTargets {
    public static final String PROVIDER = "mobileTargets";
    private MobileTargets() {}

    @DataProvider(name = PROVIDER, parallel = true)
    public static Object[][] targets() {
        return MobileTargetMatrix.from(FrameworkConfig.load()).stream()
                .map(target -> new Object[]{target})
                .toArray(Object[][]::new);
    }
}
