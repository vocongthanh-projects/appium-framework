package vn.ots.qc.mobile.cucumber;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.testng.annotations.DataProvider;

/** Safe default runner; consumers may enable parallelism only with isolated devices and forwarding ports. */
public abstract class AbstractMobileCucumberTest extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() { return super.scenarios(); }
}
