package hooks;

import bases.FunctionUltis;
import bases.User;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.cucumber.java.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Hooks {
    public static FunctionUltis functionUltis;

    private static final String SCREENSHOT_PATH = "test-output/screenshots/TTCH/";
    private static boolean isInitialized = false;
    private static AppiumDriverLocalService service;
    private static AndroidDriver driver;

    private static final String APP_PACKAGE = "com.ots.gsafe.firefighter";

    public static int totalTestCases = 0;
    public static int pass = 0;
    public static int fail = 0;
    public static int skipped = 0;

    @Before
    public void setUp(Scenario scenario) {
        try {
            if (!isInitialized) {
                initializeAppium();
                isInitialized = true;
            }

            functionUltis = new FunctionUltis(driver);

            // 🔁 Mở lại app mỗi Scenario
            if (driver.isAppInstalled(APP_PACKAGE)) {
                driver.terminateApp(APP_PACKAGE); // Đảm bảo tắt app
                Thread.sleep(1000); // Đợi app tắt hoàn toàn
            }
            driver.activateApp(APP_PACKAGE);
            // ✅ Bắt đầu login
            User.LoginHelper loginHelper = new User.LoginHelper(driver);
            loginHelper.performLogin();

            System.out.println("🚀 Bắt đầu Scenario: " + scenario.getName() + " tại " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println("❌ Lỗi trong setUp: " + e.getMessage());
            scenario.log("Lỗi khởi tạo: " + e.getMessage());
            throw new RuntimeException("Không thể khởi tạo scenario: " + e.getMessage(), e);
        }
    }






    private void initializeAppium() throws Exception {
        service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder().usingPort(4723));
        service.start();

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "emulator-5554");
        capabilities.setCapability("udid", "emulator-5554");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("platformVersion", "14");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("noReset", true);
        capabilities.setCapability("fullReset", false);
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability("autoDismissAlerts", true);

        URL url = new URL("http://127.0.0.1:4725");
        driver = new AndroidDriver(url, capabilities);
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            System.out.println("❌ Bước thất bại!");
            scenario.log("Bước thất bại tại: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else {
            System.out.println("✅ Bước thành công!");
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        String scenarioStatus = scenario.isFailed() ? "Không đạt" : "Đạt";
        totalTestCases++;
        if (scenarioStatus.equals("Đạt")) pass++;
        else if (scenarioStatus.equals("Không đạt")) fail++;
        else skipped++;

        System.out.println(scenario.isFailed() ? "🚨 Scenario thất bại: " + scenario.getName() :
                "🎉 Scenario thành công: " + scenario.getName());

        ScreenshotResult screenshotResult = captureScreenshot(driver, scenarioStatus, scenario.getName(), scenario);

        if (screenshotResult == null) {
            System.err.println("⚠️ Không thể chụp ảnh cho test case: " + scenario.getName());
            scenario.log("Không thể chụp ảnh màn hình.");
        } else {
            System.out.println("✅ Đã chụp và lưu ảnh tại: " + screenshotResult.screenshotPath);
            // Đính kèm ảnh vào Cucumber report
            scenario.attach(screenshotResult.screenshotBytes, "image/png", "Screenshot_" + scenario.getName());
        }
    }

    @AfterAll
    public static void afterAll() {
        try {
            if (isInitialized) {
                if (driver != null) {
                    driver.quit();
                }
                if (service != null && service.isRunning()) {
                    service.stop();
                }
                isInitialized = false;
                System.out.println("🛑 Appium và tài nguyên đã được đóng sau khi chạy xong tất cả Scenario");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi dọn dẹp Appium: " + e.getMessage());
        }
    }

    public static class ScreenshotResult {
        public byte[] screenshotBytes;
        public String screenshotPath;

        public ScreenshotResult(byte[] screenshotBytes, String screenshotPath) {
            this.screenshotBytes = screenshotBytes;
            this.screenshotPath = screenshotPath;
        }
    }

    public ScreenshotResult captureScreenshot(AndroidDriver driver, String scenarioStatus, String scenarioName, Scenario scenario) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
            String screenshotFileName = scenarioName.replaceAll("[\\s+]", "_") + ".png"; // Chỉ sử dụng tên scenario
            String screenshotPath = SCREENSHOT_PATH + screenshotFileName;

            File screenshotDir = new File(SCREENSHOT_PATH);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
                System.out.println("📁 Đã tạo thư mục lưu ảnh: " + SCREENSHOT_PATH);
            }

            File screenshotFile = driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            File targetFile = new File(screenshotPath);
            FileUtils.copyFile(screenshotFile, targetFile);

            byte[] screenshotBytes = Files.readAllBytes(targetFile.toPath());
            System.out.println("✅ Đã chụp và lưu ảnh tại: " + screenshotPath);

            // Đính kèm ảnh vào Cucumber report
            scenario.attach(screenshotBytes, "image/png", "Screenshot_" + scenarioName);

            return new ScreenshotResult(screenshotBytes, screenshotPath);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi chụp ảnh màn hình: " + e.getMessage());
            scenario.log("Lỗi khi chụp ảnh màn hình: " + e.getMessage());
            return null;
        }
    }
}