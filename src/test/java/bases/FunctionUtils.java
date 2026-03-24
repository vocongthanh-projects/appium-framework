package bases;

import hooks.TestContextProvider;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Intelligent FunctionUtils - Optimized for UiSelector.
 * Now uses a smart native polling engine to evaluate List<By> securely.
 */
public class FunctionUtils {
    private final HashSet<String> generatedPhones = new HashSet<>();

    public FunctionUtils() {}

    private AndroidDriver driver() {
        return TestContextProvider.getContext().getDriver();
    }

    // --- CORE LOCATOR ENGINE ---

    /**
     * Tự động quét (poll) qua một danh sách các By (UiSelectors ưu tiên cao).
     * Trả về ngay khi khớp phần tử đầu tiên hiển thị trên màn hình.
     */
    private WebElement waitForElement(List<By> locators, int timeoutSeconds) {
        AndroidDriver driver = driver();
        long end = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        
        // Tạm thời Disable Implicit Wait để tránh bị treo 15s cho từng selector sai
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); 
        
        try {
            while (System.currentTimeMillis() < end) {
                for (By loc : locators) {
                    try {
                        List<WebElement> els = driver.findElements(loc);
                        if (!els.isEmpty() && els.get(0).isDisplayed()) {
                            return els.get(0); // Trả về nếu tìm thấy phần tử hiển thị
                        }
                    } catch (Exception ignored) {} // Bỏ qua selector lỗi
                }
                Thread.sleep(500); // Poll ngắt quãng mỗi 0.5s để bảo vệ tài nguyên
            }
        } catch (InterruptedException ignored) {} finally {
            // Restore lại Implicit Wait
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getDefaultTimeout())); 
        }
        throw new NoSuchElementException("❌ Không tìm thấy phần tử qua bất kỳ bộ lọc UiSelector nào: " + locators.toString());
    }

    // --- SMART INTERACTION API ---

    public boolean isVisible(List<By> locators) {
        return isVisible(locators, ConfigManager.getDefaultTimeout());
    }

    public boolean isVisible(List<By> locators, int timeoutSeconds) {
        try {
            waitForElement(locators, timeoutSeconds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVisible(By locator) {
        return isVisible(List.of(locator));
    }

    public boolean isVisible(By locator, int timeoutSeconds) {
        return isVisible(List.of(locator), timeoutSeconds);
    }

    public void click(String text) {
        clickElement(XpathList.getPriorityLocators(text));
    }

    public void clickButton(String text) {
        clickElement(XpathList.getButtonLocators(text));
    }

    public void clickElement(List<By> locators) {
        WebElement el = waitForElement(locators, ConfigManager.getDefaultTimeout());
        el.click();
        System.out.println("✅ Clicked using fallback locators.");
    }

    public void clickElement(By locator) {
        clickElement(List.of(locator));
    }

    public void type(String label, String value) {
        typeElement(XpathList.getInputLocators(label), value);
    }

    public void typeElement(By locator, String value) {
        typeElement(List.of(locator), value);
    }

    public void typeElement(List<By> locators, String value) {
        WebElement el = waitForElement(locators, ConfigManager.getDefaultTimeout());
        el.clear();
        el.sendKeys(value);
        hideKeyboard();
        System.out.println("✅ Typed info using fallback locators.");
    }


    // --- VERIFICATION API ---

    public void shouldBeVisible(String text) {
        if (!isVisible(XpathList.getPriorityLocators(text))) {
            throw new RuntimeException("❌ Phần tử bị ẩn hoặc không xuất hiện: " + text);
        }
        System.out.println("🔍 Xác minh thành công hiển thị: " + text);
    }

    public void shouldNotBeVisible(String text) {
        if (isVisible(XpathList.getPriorityLocators(text), 2)) {
            throw new RuntimeException("❌ Phần tử vẫn còn hiển thị, trái với kết quả mong đợi: " + text);
        }
        System.out.println("🔍 Đã xác định là biến mất: " + text);
    }


    // --- MOBILE UTILS ---

    public void scrollIntoView(String text) {
        driver().findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"" + text + "\"))"));
    }

    public void swipeDown() {
        AndroidDriver driver = driver();
        Dimension dim = driver.manage().window().getSize();
        int x = dim.width / 2;
        int startY = (int) (dim.height * 0.7);
        int endY = (int) (dim.height * 0.3);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), x, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }

    public void hideKeyboard() {
        AndroidDriver driver = driver();
        try { if (driver.isKeyboardShown()) driver.hideKeyboard(); } catch (Exception ignored) {}
    }

    public void typeRandomPhone(String label) {
        Random rand = new Random();
        String phone;
        do {
            StringBuilder sb = new StringBuilder("03");
            for (int i = 0; i < 8; i++) sb.append(rand.nextInt(10));
            phone = sb.toString();
        } while (generatedPhones.contains(phone));
        generatedPhones.add(phone);
        type(label, phone);
    }

    public void selectFromDropdown(String label, String option) {
        clickElement(XpathList.getDropdownLocators(label));
        clickElement(XpathList.getPriorityLocators(option));
    }

    public void shouldBeEnabled(String text) {
        WebElement el = waitForElement(XpathList.getPriorityLocators(text), ConfigManager.getDefaultTimeout());
        if (!el.isEnabled()) {
            throw new RuntimeException("❌ Elements không cho phép tương tác (Not enabled): " + text);
        }
    }

    public void shouldBeDisabled(String text) {
        WebElement el = waitForElement(XpathList.getPriorityLocators(text), ConfigManager.getDefaultTimeout());
        if (el.isEnabled()) {
            throw new RuntimeException("❌ Elements vẫn đang sáng đèn (Not disabled): " + text);
        }
    }
}