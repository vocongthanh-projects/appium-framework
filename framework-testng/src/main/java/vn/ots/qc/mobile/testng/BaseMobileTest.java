package vn.ots.qc.mobile.testng;

import io.appium.java_client.AppiumDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import vn.ots.qc.mobile.context.MobileTestContext;
import vn.ots.qc.mobile.evidence.MobileEvidence;
import vn.ots.qc.mobile.interaction.ElementActions;
import vn.ots.qc.mobile.interaction.Gestures;
import vn.ots.qc.mobile.interaction.MobileApplication;
import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.device.DeviceTarget;

public abstract class BaseMobileTest {
    public static final String EVIDENCE_ATTRIBUTE = "appium.evidence";
    private final ThreadLocal<MobileTestContext> current = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public final void startMobileTest(Object[] testData) {
        DeviceTarget target = java.util.Arrays.stream(testData == null ? new Object[0] : testData)
                .filter(DeviceTarget.class::isInstance).map(DeviceTarget.class::cast).findFirst().orElse(null);
        MobileTestContext context = target == null
                ? new MobileTestContext()
                : new MobileTestContext(target.applyTo(FrameworkConfig.load()));
        current.set(context);
        try { context.start(); }
        catch (RuntimeException exception) { context.close(); current.remove(); throw exception; }
    }

    @AfterMethod(alwaysRun = true)
    public final void finishMobileTest(ITestResult result) {
        MobileTestContext context = current.get();
        if (context == null) return;
        try {
            MobileEvidence evidence = context.finish(result.getMethod().getQualifiedName(),
                    result.getStatus() != ITestResult.SUCCESS);
            result.setAttribute(EVIDENCE_ATTRIBUTE, evidence);
            if (evidence.screenshot() != null) Reporter.log("Screenshot: " + evidence.screenshot(), true);
            if (evidence.pageSource() != null) Reporter.log("Page source: " + evidence.pageSource(), true);
        } finally { current.remove(); }
    }

    protected final MobileTestContext mobile() {
        return java.util.Objects.requireNonNull(current.get(), "No active mobile test context");
    }
    protected final AppiumDriver driver() { return mobile().driver(); }
    protected final ElementActions actions() { return mobile().actions(); }
    protected final Gestures gestures() { return mobile().gestures(); }
    protected final MobileApplication application() { return mobile().application(); }
}
