package bases;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class User {
    private static String username = "";
    private static String password = "";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static class LoginHelper {
        private final AndroidDriver driver;
        private final FunctionUltis functionsUltis;

        public LoginHelper(AndroidDriver driver) {
            this.driver = driver;
            this.functionsUltis = new FunctionUltis(driver);
        }


        public void performLogin() {
            if (isLoggedIn()) {
                System.out.println("✅ Người dùng đã đăng nhập. Bỏ qua bước đăng nhập.");
                return;
            }

            System.out.println("🔐 Thực hiện đăng nhập với username: " + username + " và password: " + password);

            int maxRetries = 3;
            int retryCount = 0;
            int waitBetweenRetries = 2000; // 2 giây giữa các lần thử

            while (retryCount < maxRetries) {
                try {
                    // Nhập tài khoản
                    functionsUltis.clickAndInputByLocator(By.id("com.ots.gsafe.firefighter:id/edtPhone"), "0354078927");

                    // Nhập mật khẩu
                    functionsUltis.clickAndInputByLocator(By.id("com.ots.gsafe.firefighter:id/edtPassword"), "Pc07@1234567");

                    // Đăng nhập
                    functionsUltis.clickElementByLocator(By.id("com.ots.gsafe.firefighter:id/button"));

                    // Kiểm tra thành công
                    if (isLoggedIn()) {
                        System.out.println("✅ Đăng nhập thành công!");
                        return;
                    } else {
                        throw new RuntimeException("Đăng nhập thất bại, không tìm thấy Trang chủ.");
                    }
                } catch (Exception e) {
                    retryCount++;
                    System.err.println("❌ Lỗi khi đăng nhập (lần " + retryCount + "): " + e.getMessage());
                    if (retryCount == maxRetries) {
                        throw new RuntimeException("Không thể đăng nhập sau " + maxRetries + " lần thử: " + e.getMessage(), e);
                    }
                    try {
                        Thread.sleep(waitBetweenRetries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        /**
         * Kiểm tra người dùng đã đăng nhập chưa
         */
        public boolean isLoggedIn() {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement homeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.id("com.ots.gsafe.firefighter:id/navigation_bar_item_large_label_view")));
                return homeElement.isDisplayed();
            } catch (TimeoutException | NoSuchElementException e) {
                return false;
            }
        }
    }
}
