package tests.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import bases.ConfigManager;
import java.io.File;

/**
 * Optimized TestRunner - Clean and stable for Cucumber 7.x execution.
 * Removed custom matrix wrapping to resolve NullPointer in runScenario.
 */
@CucumberOptions(
        features = "src/test/java/feature/account/Logout.feature",
        glue = {"hooks", "stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @BeforeSuite
    public void setUpSuite() {
        System.out.println("🧹 ===== STARTING CLEANUP BEFORE TEST SUITE =====");
        clearResults();
        System.out.println("✅ ===== CLEANUP COMPLETE =====\n");
    }

    private void clearResults() {
        deleteDir(new File("target/cucumber-reports/"));
        deleteDir(new File("allure-results/"));
    }

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    @AfterSuite
    public void tearDownSuite() {
        System.out.println("📊 Test Execution completed. Allure results generated in allure-results/");
    }
}
