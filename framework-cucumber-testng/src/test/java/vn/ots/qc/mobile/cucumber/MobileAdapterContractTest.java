package vn.ots.qc.mobile.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public final class MobileAdapterContractTest {
    @Test public void ownsBoundaryHooksAndUsesSafeSerialDefault() throws Exception {
        assertEquals(MobileLifecycleHooks.class.getMethod("startSession").getAnnotation(Before.class).order(),
                Integer.MIN_VALUE);
        assertEquals(MobileLifecycleHooks.class.getMethod("finishSession", io.cucumber.java.Scenario.class)
                .getAnnotation(After.class).order(), Integer.MIN_VALUE);
        assertFalse(AbstractMobileCucumberTest.class.getMethod("scenarios")
                .getAnnotation(DataProvider.class).parallel());
        assertTrue(AbstractMobileMatrixCucumberTest.class.getMethod("scenarios")
                .getAnnotation(DataProvider.class).parallel());
    }
}
