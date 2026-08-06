package vn.ots.qc.mobile.testng;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public final class BaseMobileTestContractTest {
    @Test public void lifecycleAlwaysRuns() throws Exception {
        assertTrue(BaseMobileTest.class.getMethod("startMobileTest", Object[].class)
                .getAnnotation(BeforeMethod.class).alwaysRun());
        assertTrue(BaseMobileTest.class.getMethod("finishMobileTest", org.testng.ITestResult.class)
                .getAnnotation(AfterMethod.class).alwaysRun());
        assertTrue(MobileTargets.class.getMethod("targets").getAnnotation(org.testng.annotations.DataProvider.class).parallel());
    }
}
