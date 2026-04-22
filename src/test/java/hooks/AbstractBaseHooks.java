package hooks;

import bases.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.cucumber.java.Scenario;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import pages.LoginPage;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Modern AbstractBaseHooks - Finalized for Appium 9+ and Selenium 4.22+.
 * Uses UiAutomator2Options for standardized session management.
 */
public abstract class AbstractBaseHooks {

    private static final ThreadLocal<AppiumDriverLocalService> serviceThread = new ThreadLocal<>();
    private static final ThreadLocal<AndroidDriver> driverThread = new ThreadLocal<>();
    protected Scenario currentScenario;

    protected void setUpBase(Scenario scenario) {
        this.currentScenario = scenario;
        System.out.println("🔧 Setting up Scenario: " + scenario.getName());
        
        try {
            if (serviceThread.get() == null) {
                int port = ConfigManager.getAppiumPort();
                if (port <= 0) {
                    throw new RuntimeException("❌ Fatal: 'appium.server.port' is missing or invalid in config.properties!");
                }
                
                AppiumDriverLocalService service = AppiumDriverLocalService.buildService(
                        new AppiumServiceBuilder().withIPAddress("127.0.0.1").usingPort(port));
                service.start();
                serviceThread.set(service);
                System.out.println("🚀 Appium Service strictly started on Config Port: " + port);
            }

            if (driverThread.get() == null) {
                int currentPort = serviceThread.get().getUrl().getPort();
                URL appiumUrl = URI.create("http://127.0.0.1:" + currentPort).toURL();
                
                System.out.println("📱 Starting AndroidDriver (v9) on: " + appiumUrl);
                AndroidDriver driver = new AndroidDriver(appiumUrl, buildOptions());
                driverThread.set(driver);
                TestContextProvider.setContext(driver);
            }

            AndroidDriver driver = driverThread.get();
            String pkg = ConfigManager.getAppPackage();

            if (driver.isAppInstalled(pkg)) {
                driver.terminateApp(pkg);
            }
            driver.activateApp(pkg);

            if (scenario.getSourceTagNames().contains("@login")) {
                new LoginPage(driver).login();
            }
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in setUpBase: " + e.getMessage());
            throw new RuntimeException("Environment setup failed. Check Appium/Device status.", e);
        }
    }

    /**
     * Build modern UiAutomator2Options for Appium 9+ compatibility.
     */
    protected UiAutomator2Options buildOptions() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setUdid(ConfigManager.getUdid())
                .setAppPackage(ConfigManager.getAppPackage())
                .setNoReset(ConfigManager.getBoolean("android.no.reset"))
                .setFullReset(ConfigManager.getBoolean("android.full.reset"))
                .setAutoGrantPermissions(ConfigManager.getBoolean("android.auto.grant.permissions"))
                .setNewCommandTimeout(Duration.ofSeconds(120))
                .setAdbExecTimeout(Duration.ofSeconds(30));

        return options;
    }

    protected void tearDownBase(Scenario scenario) {
        AndroidDriver driver = driverThread.get();
        if (driver != null) {
            captureScreenshot(driver, scenario.isFailed() ? "FAILED" : "PASSED", scenario.getName());
        }
    }

    protected static void shutDownBase() {
        if (driverThread.get() != null) {
            driverThread.get().quit();
            driverThread.remove();
        }
        if (serviceThread.get() != null) {
            serviceThread.get().stop();
            serviceThread.remove();
        }
        TestContextProvider.clear();
    }

    protected void captureScreenshot(AndroidDriver driver, String status, String scenarioName) {
        try {
            String sanitizedName = scenarioName.replaceAll("[^a-zA-Z0-9]", "_");
            String profile = currentScenario.getSourceTagNames().contains("@login") ? "login" : "notlogin";
            String safeConfig = ConfigManager.getUdid().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();


            String folderPath = ConfigManager.getScreenshotPath() + profile + "/" + safeConfig + "/";
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            String path = folderPath + sanitizedName + "_" + status + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".png";
            File capture = Objects.requireNonNull(driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(capture, new File(path));

            currentScenario.attach(Files.readAllBytes(new File(path).toPath()), "image/png", "Screenshot_" + sanitizedName);
        } catch (Exception e) {
            System.err.println("⚠️ Screenshot capture failed: " + e.getMessage());
        }
    }
}
