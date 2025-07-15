package reports.reports;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class ExtentReportListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(ExtentReportListener.class);

    @Override
    public void onStart(ITestContext context) {
        try {
            ExtentManager.setExtent();
        } catch (IOException e) {
            logger.error("Error initializing ExtentReports", e);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        if (!testName.contains("runScenario")) {  // Bỏ qua runScenario
            ExtentManager.createTest(result.getName());
            logger.info("Starting test: " + result.getName());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getTest().log(Status.PASS, "✅ Passed: " + result.getName());
        attachLogFile();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentManager.getTest().log(Status.FAIL, MarkupHelper.createLabel("❌ Failed: " + result.getName(), ExtentColor.RED));
        ExtentManager.getTest().log(Status.FAIL, "Error Message: " + result.getThrowable());
        attachLogFile();
    }

    @Override
    public void onFinish(ITestContext context) {
        int passedTests = context.getPassedTests().size();
        int failedTests = context.getFailedTests().size();
        ExtentManager.updateTestSummary(passedTests, failedTests);
        ExtentManager.endReport();
    }

    // 🆕 Ghi log từng step từ Cucumber vào Extent Report
    public static void logScenarioStep(Scenario scenario, String step) {
        if (scenario.isFailed()) {
            ExtentManager.getTest().log(Status.FAIL, "❌ Bước thất bại: " + step);
        } else {
            ExtentManager.getTest().log(Status.PASS, "✅ Bước thành công: " + step);
        }
    }

    private void attachLogFile() {
        try {
            File logFile = new File("test-output/logs/test-execution.log");
            if (logFile.exists()) {
                String logContent = Files.readAllLines(logFile.toPath()).stream().collect(Collectors.joining("\n"));
                ExtentManager.getTest().info("📜 Test Execution Logs:");
                ExtentManager.getTest().info("<pre>" + logContent + "</pre>");
            }
        } catch (Exception e) {
            logger.error("Error attaching log file", e);
        }
    }
}
