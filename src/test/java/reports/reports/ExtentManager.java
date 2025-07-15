package reports.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.IOException;


public class ExtentManager {
    public static ExtentSparkReporter sparkReporter;
    public static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static void setExtent() throws IOException {
        sparkReporter = new ExtentSparkReporter(
                System.getProperty("user.dir") + "/test-output/ExtentReport/"
                        + "TestExecutionReport"
                        + ".html");
        sparkReporter.loadXMLConfig(System.getProperty("user.dir")
                + "/src/test/java/resources/extent-config.xml");


        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
    }

    public static ExtentTest createTest(String testName) {
        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
        return extentTest;
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void logStep(String message) {
        getTest().info(message);
    }

    public static void updateTestSummary(int passedTests, int failedTests) {
        int totalTests = passedTests + failedTests;

        extent.setSystemInfo("✅ Passed Tests", String.valueOf(passedTests));
        extent.setSystemInfo("❌ Failed Tests", String.valueOf(failedTests));
        extent.setSystemInfo("📌 >>> Total Tests", String.valueOf(totalTests));
    }

    public static void endReport() {
        extent.flush();
    }


}
