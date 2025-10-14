package bases;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class FunctionUltis {
    private AndroidDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private HashSet<String> generatedPhoneNumbers = new HashSet<>(); // Lưu trữ các số điện thoại đã tạo

    public FunctionUltis(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        this.js = (JavascriptExecutor) this.driver;
    }

    // Phương thức click tổng quát dựa trên văn bản
    public WebElement waitForElement(By locator, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void clickElement(String identifier) {
        if (driver == null) {
            throw new IllegalStateException("AndroidDriver chưa được khởi tạo.");
        }

        By locator = getLocatorByText(identifier);
        int maxRetries = 3;
        int retryCount = 0;
        int waitBetweenRetries = 1000; // 1 giây

        while (retryCount < maxRetries) {
            try {
                // ⏳ Chờ phần tử có trong DOM
                waitForElement(locator, 5000);

                // ⏳ Chờ element sẵn sàng để click
                WebElement element = waitUntilElementClickable(locator, 5000);
                element.click();

                System.out.println("🟢 Clicked on element with text: " + identifier);
                return;

            } catch (Exception e) {
                retryCount++;
                System.out.println("⚠️ Lần thử click " + retryCount + " thất bại cho element: " + identifier);

                if (retryCount >= maxRetries) {
                    throw new RuntimeException("❌ Không thể nhấn phần tử '" + identifier
                            + "' sau " + maxRetries + " lần thử: " + e.getMessage());
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
     * Polling chờ element có thể click được
     */
    private WebElement waitUntilElementClickable(By locator, int timeoutSeconds) {
        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            try {
                WebElement element = findUniqueElement(locator);
                if (element != null && element.isDisplayed() && element.isEnabled()) {
                    return element; // ✅ Sẵn sàng click
                }
            } catch (Exception ignored) {
                // Bỏ qua và retry
            }

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        throw new RuntimeException("❌ Element không thể click trong " + timeoutSeconds + "s: " + locator);
    }


    public void clickElementByLocator(By locator) {
        if (driver == null) return;

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                waitForElementToBeVisible(locator);
                Thread.sleep(10);
                WebElement webElement = driver.findElement(locator);

                if (webElement.isDisplayed() && webElement.isEnabled()) {
                    Thread.sleep(10);
                    webElement.click();
                    break; // Nhấn thành công, thoát khỏi vòng lặp
                }
            } catch (Exception e) {
                retryCount++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private WebElement waitForElementToBeStable(By locator, int checkIntervalMs, int stableCountRequired) {
        WebElement element = null;
        int stableCount = 0;
        org.openqa.selenium.Rectangle lastRect = null;

        for (int i = 0; i < 10; i++) { // Tối đa 10 lần kiểm tra (≈10s nếu checkIntervalMs = 1000)
            try {
                element = driver.findElement(locator);
                org.openqa.selenium.Rectangle currentRect = element.getRect();

                if (lastRect != null && currentRect.equals(lastRect)) {
                    stableCount++;
                    if (stableCount >= stableCountRequired) {
                        System.out.println("🟢 Element ổn định sau " + (i + 1) + " lần kiểm tra.");
                        return element;
                    }
                } else {
                    stableCount = 0; // Reset nếu element thay đổi vị trí/kích thước
                }

                lastRect = currentRect;
                Thread.sleep(checkIntervalMs);
            } catch (Exception e) {
                System.out.println("⏳ Đang chờ element ổn định: " + e.getMessage());
                try {
                    Thread.sleep(checkIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException("Element không ổn định sau nhiều lần kiểm tra: " + locator);
    }


    public void clickAndInputByLocator(By locator, String inputText) {
        if (driver == null) return;

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(10);
                WebElement element = driver.findElement(locator);

                if (element.isDisplayed() && element.isEnabled()) {
                    Thread.sleep(10);
                    clearAndInputText(element, inputText);
                    closeKeyboard();
                    break; // Thao tác thành công, thoát khỏi vòng lặp
                }
            } catch (Exception e) {
                retryCount++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    // Phương thức input tổng quát dựa trên văn bản
    public void clickAndInput(String identifier, String text) {
        if (driver == null) {
            throw new IllegalStateException("AndroidDriver chưa được khởi tạo.");
        }

        By locator = getLocatorByText(identifier);
        int maxRetries = 3;
        int retryCount = 0;
        int waitBetweenRetries = 1000;

        while (retryCount < maxRetries) {
            try {
                // 🕒 Chờ phần tử hiển thị & ổn định
                waitForElementToBeVisible(locator);
                WebElement element = waitForElementToBeStable(locator, 1000, 3);

                if (element != null && element.isDisplayed() && element.isEnabled()) {
                    clearAndInputText(element, text);
                    closeKeyboard();
                    System.out.println("✅ Đã nhập text '" + text + "' vào element: " + identifier);
                    return;
                } else {
                    throw new RuntimeException("Phần tử không sẵn sàng để nhập liệu: " + identifier);
                }

            } catch (Exception e) {
                retryCount++;
                System.out.println("⚠️ Lần thử thứ " + retryCount + " thất bại khi nhập '" + identifier + "': " + e.getMessage());
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("❌ Không thể nhập liệu vào '" + identifier + "' sau " + maxRetries + " lần thử: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(waitBetweenRetries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    // Tạo locator dựa trên văn bản với fallback
    private By getLocatorByText(String identifier) {
        By locator = By.xpath("//*[@text='" + identifier + "']");
        if (!isElementPresent(locator)) {
            System.out.println("Fallback to contains for text: " + identifier);
            locator = By.xpath("//*[contains(@text, '" + identifier + "')]");
        }
        if (!isElementPresent(locator)) {
            throw new RuntimeException("Không tìm thấy phần tử với text: " + identifier);
        }
        return locator;
    }

    // Tìm phần tử duy nhất để tránh xung đột
    private WebElement findUniqueElement(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        if (elements.size() > 1) {
            throw new RuntimeException("Tìm thấy nhiều phần tử với text '" + locator + "'. Vui lòng kiểm tra lại.");
        }
        if (elements.isEmpty()) {
            throw new RuntimeException("Không tìm thấy phần tử với text: " + locator);
        }
        return elements.get(0);
    }

    // Kiểm tra sự tồn tại của phần tử
    private boolean isElementPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra phần tử có hiển thị không
    public boolean isElementDisplayed(String identifier) {
        try {
            By locator = getLocatorByText(identifier);
            waitForElementToBeVisible(locator);
            WebElement element = findUniqueElement(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            throw new RuntimeException("Không thể kiểm tra hiển thị của phần tử '" + identifier + "': " + e.getMessage());
        }
    }

    // Kiểm tra văn bản hiển thị
    public boolean verifyTextDisplayed(String expectedText) {
        try {
            String normalizedExpected = expectedText.trim();
            System.out.println("🔎 Đang xác minh text hiển thị: " + normalizedExpected);

            // Danh sách locator fallback theo thứ tự ưu tiên
            By[] locators = new By[]{
                    By.xpath("//*[@text='" + normalizedExpected + "']"),
                    By.xpath("//*[contains(@text, '" + normalizedExpected + "')]"),
                    By.xpath("//*[contains(@label, '" + normalizedExpected + "')]"),
                    By.xpath("//*[contains(@name, '" + normalizedExpected + "')]"),
                    By.xpath("//*[contains(@content-desc, '" + normalizedExpected + "')]"),
                    By.xpath("//*[normalize-space(text())='" + normalizedExpected + "']"),
                    By.xpath("//*[contains(normalize-space(text()), '" + normalizedExpected + "')]")
            };

            WebElement foundElement = null;

            for (By locator : locators) {
                try {
                    System.out.println("🕐 Thử tìm với locator: " + locator);

                    // ⏳ Chờ tối đa 10 giây để element tồn tại
                    waitForElement(locator, 10);

                    // ⏳ Chờ element hiển thị
                    waitForElementToBeVisible(locator);

                    // 🔁 Retry trong 3 lần (phòng trường hợp render chậm)
                    int retry = 0;
                    while (retry < 3) {
                        List<WebElement> elements = driver.findElements(locator);
                        for (WebElement element : elements) {
                            String actualText = element.getText();
                            if (actualText != null && actualText.trim().contains(normalizedExpected)) {
                                System.out.println("✅ Found text: '" + normalizedExpected + "' via " + locator);
                                foundElement = element;
                                break;
                            }
                        }
                        if (foundElement != null) break;

                        retry++;
                        Thread.sleep(1000); // chờ thêm 1s rồi thử lại
                        System.out.println("⏳ Chờ text hiển thị, lần thử: " + retry);
                    }

                    if (foundElement != null) break;

                } catch (Exception ignored) {
                    // Tiếp tục thử locator khác
                }
            }

            if (foundElement == null) {
                throw new Exception("Không tìm thấy văn bản: " + normalizedExpected);
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException("❌ Không thể xác minh văn bản hiển thị: " + e.getMessage());
        }
    }



    // Phương thức tạo số điện thoại ngẫu nhiên bắt đầu bằng "03"
    public String generateRandomPhoneNumber() {
        Random random = new Random();
        StringBuilder phoneNumber = new StringBuilder("03");

        // Tạo 8 số ngẫu nhiên
        for (int i = 0; i < 8; i++) {
            phoneNumber.append(random.nextInt(10));
        }

        String result = phoneNumber.toString();

        // Kiểm tra xem số điện thoại đã tồn tại chưa
        while (generatedPhoneNumbers.contains(result)) {
            phoneNumber = new StringBuilder("03");
            for (int i = 0; i < 8; i++) {
                phoneNumber.append(random.nextInt(10));
            }
            result = phoneNumber.toString();
        }

        // Lưu số điện thoại mới vào HashSet
        generatedPhoneNumbers.add(result);
        System.out.println("Generated phone number: " + result);
        return result;
    }

    // Phương thức nhập số điện thoại ngẫu nhiên vào trường input
    public void inputRandomPhoneNumber(String identifier) {
        String phoneNumber = generateRandomPhoneNumber();
        clickAndInput(identifier, phoneNumber);
    }

    // Kiểm tra nhiều văn bản hiển thị đúng
    public void verifyMultipleTextsDisplayed(List<String> expectedTexts) {
        try {
            boolean allTextsFound = true;
            StringBuilder errorMessage = new StringBuilder();

            for (String expectedText : expectedTexts) {
                try {
                    String safeText = buildSafeXpathTextCondition(expectedText);
                    By locator = By.xpath("//*[@text=" + safeText + "]");
                    boolean found = false;

                    try {
                        // ⏳ Chờ element xuất hiện trong DOM
                        waitForElement(locator, 5);
                        // ⏳ Chờ element hiển thị
                        waitForElementToBeVisible(locator);
                        found = waitWithPolling(locator, expectedText, 5);
                    } catch (Exception e) {
                        // Nếu hết timeout mà vẫn chưa có -> fallback sang contains
                        System.out.println("Fallback to contains for verifying text: " + expectedText);
                        locator = By.xpath("//*[contains(@text, " + safeText + ")]");
                        waitForElement(locator, 5);
                        waitForElementToBeVisible(locator);
                        found = waitWithPolling(locator, expectedText, 5);
                    }

                    if (!found) {
                        allTextsFound = false;
                        errorMessage.append("Text not found: ").append(expectedText).append("; ");
                    } else {
                        System.out.println("🟢 Found text '" + expectedText + "' in DOM.");
                    }

                } catch (Exception e) {
                    allTextsFound = false;
                    errorMessage.append("Error verifying text '")
                            .append(expectedText).append("': ")
                            .append(e.getMessage()).append("; ");
                }
            }

            if (!allTextsFound) {
                throw new RuntimeException("Không thể xác minh các văn bản hiển thị: " + errorMessage.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra các văn bản hiển thị: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý an toàn chuỗi khi build XPath để tránh lỗi khi có dấu nháy đơn / kép.
     */
    private String buildSafeXpathTextCondition(String expectedText) {
        if (expectedText.contains("'") && expectedText.contains("\"")) {
            // Nếu chứa cả hai loại dấu nháy -> dùng concat()
            String[] parts = expectedText.split("'");
            StringBuilder concatBuilder = new StringBuilder("concat(");
            for (int i = 0; i < parts.length; i++) {
                concatBuilder.append("'").append(parts[i]).append("'");
                if (i != parts.length - 1) concatBuilder.append(", \"'\", ");
            }
            concatBuilder.append(")");
            return concatBuilder.toString();
        } else if (expectedText.contains("'")) {
            // Nếu có dấu nháy đơn -> bao bằng dấu nháy kép
            return "\"" + expectedText + "\"";
        } else {
            // Mặc định bao bằng dấu nháy đơn
            return "'" + expectedText + "'";
        }
    }



    /**
     * Polling để chờ text hiển thị trong khoảng timeout
     */
    private boolean waitWithPolling(By locator, String expectedText, int timeoutSeconds) {
        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement element : elements) {
                    if (element.getText() != null && element.getText().trim().equals(expectedText)) {
                        return true; // ✅ tìm thấy
                    }
                }
            } catch (Exception ignored) {
                // bỏ qua, retry
            }

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        return false; // ❌ hết timeout vẫn chưa tìm thấy
    }


    // Kiểm tra phần tử bị vô hiệu hóa
    public boolean verifyElementDisabled(String identifier) {
        try {
            By locator = getLocatorByText(identifier);
            waitForElementToBeVisible(locator);
            WebElement element = findUniqueElement(locator);
            String elementType = element.getTagName() != null ? element.getTagName().toLowerCase() : "";
            if (elementType.contains("button") || elementType.contains("btn")) {
                return verifyButtonDisabled(element);
            } else if (elementType.contains("input") || elementType.contains("textfield")) {
                return verifyInputDisabled(element);
            }
            return !element.isEnabled();
        } catch (Exception e) {
            throw new RuntimeException("Không thể xác minh trạng thái vô hiệu hóa của phần tử '" + identifier + "': " + e.getMessage());
        }
    }

    // Kiểm tra phần tử được kích hoạt
    public boolean verifyElementEnabled(String identifier) {
        try {
            By locator = getLocatorByText(identifier);
            waitForElementToBeVisible(locator);
            WebElement element = findUniqueElement(locator);
            return element.isEnabled();
        } catch (Exception e) {
            throw new RuntimeException("Không thể xác minh trạng thái kích hoạt của phần tử '" + identifier + "': " + e.getMessage());
        }
    }



    // Kiểm tra văn bản của phần tử
    public void assertElementText(String identifier, String expectedText) {
        try {
            By locator = getLocatorByText(identifier);
            waitForElementToBeVisible(locator);
            WebElement element = findUniqueElement(locator);
            String actual = element.getText().trim();
            if (!actual.equals(expectedText.trim())) {
                throw new Exception("Văn bản không khớp. Kỳ vọng: \"" + expectedText + "\", Thực tế: \"" + actual + "\"");
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể xác minh văn bản của phần tử '" + identifier + "': " + e.getMessage());
        }
    }

    // Cuộn đến phần tử với text
    public void scrollToElementMobile(String text) {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).setMaxSearchSwipes(10).scrollIntoView(new UiSelector().text(\"" + text + "\"))"));
            System.out.println("Scrolled to element with text: " + text);
        } catch (Exception e) {
            System.out.println("Không thể cuộn đến phần tử với text: " + text);
            throw new RuntimeException("Lỗi khi cuộn đến phần tử: " + e.getMessage());
        }
    }

    // Cuộn xuống một lần
    public void scrollDownOnce() {
        try {
            Dimension dimension = driver.manage().window().getSize();
            int scrollStartY = (int) (dimension.getHeight() * 0.7);
            int scrollEndY = (int) (dimension.getHeight() * 0.3);
            int centerX = dimension.getWidth() / 2;

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, scrollStartY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), centerX, scrollEndY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            driver.perform(Arrays.asList(swipe));
            System.out.println("Scrolled down once.");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cuộn xuống: " + e.getMessage(), e);
        }
    }


    // Cuộn xuống một đoạn ngắn (dùng W3C Actions)
    public void scrollDownSmall() {
        try {
            Dimension dimension = driver.manage().window().getSize();
            int scrollStartY = (int) (dimension.getHeight() * 0.6);
            int scrollEndY = (int) (dimension.getHeight() * 0.4);
            int centerX = dimension.getWidth() / 2;

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, scrollStartY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(400), PointerInput.Origin.viewport(), centerX, scrollEndY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            driver.perform(Arrays.asList(swipe));
            System.out.println("Scrolled down small.");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cuộn xuống nhỏ: " + e.getMessage(), e);
        }
    }


    // Chờ phần tử hiển thị
    private boolean waitForElementToBeVisible(By locator) {
        try {
            System.out.println("Timeout: Start " + System.currentTimeMillis());
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            System.out.println("Timeout: End " + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi khi chờ phần tử hiển thị: " + e.getMessage());
            return false;
        }
    }

    // Xóa và nhập liệu vào phần tử
    private void clearAndInputText(WebElement element, String inputText) {
        try {
            String currentText = element.getAttribute("text");
            if (currentText != null && !currentText.isEmpty()) {
                element.clear();
            }
            element.sendKeys(inputText);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa và nhập liệu: " + e.getMessage());
        }
    }

    // Kiểm tra văn bản không hiển thị nữa
    public boolean verifyTextNotDisplayed(String text) {
        try {
            By locator = By.xpath("//*[@text='" + text + "']");
            if (!isElementPresent(locator)) {
                System.out.println("🟢 Text '" + text + "' is not displayed as expected.");
                return true;
            }
            // Nếu không tìm thấy bằng text chính xác, thử tìm bằng contains
            locator = By.xpath("//*[contains(@text, '" + text + "')]");
            if (!isElementPresent(locator)) {
                System.out.println("🟢 Text '" + text + "' is not displayed as expected (contains check).");
                return true;
            }
            // Nếu vẫn tìm thấy phần tử
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                if (element.isDisplayed() && element.getText() != null && element.getText().trim().equals(text)) {
                    throw new RuntimeException("Text '" + text + "' is still displayed.");
                }
            }
            System.out.println("🟢 Text '" + text + "' is not displayed as expected.");
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error while verifying text '" + text + "' is not displayed: " + e.getMessage());
        }
    }

    // Đóng bàn phím
    public void closeKeyboard() {
        try {
            if (driver.isKeyboardShown()) {
                driver.hideKeyboard();
            }
        } catch (Exception e) {
            System.out.println("Không thể đóng bàn phím: " + e.getMessage());
        }
    }

    // Kiểm tra nút bị vô hiệu hóa
    private boolean verifyButtonDisabled(WebElement element) {
        try {
            return !element.isEnabled() || element.getAttribute("enabled").equals("false");
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra trường nhập liệu bị vô hiệu hóa
    private boolean verifyInputDisabled(WebElement element) {
        try {
            return !element.isEnabled() || element.getAttribute("editable").equals("false");
        } catch (Exception e) {
            return false;
        }
    }

    // Phương thức clickUntilElementAppearsNew
    public void clickUntilElementAppearsNew(String backButtonIdentifier, int maxRetries) {
        int retries = 0;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        By backButtonLocator = getLocatorByText(backButtonIdentifier);

        while (retries < maxRetries) {
            try {
                scrollToElementMobile("Mới");
                clickElement("Mới");
                if (isElementPresent(By.xpath("//*[@resource-id='com.ots.c08.vnecsgt.cbcs:id/btnSend']"))) {
                    System.out.println("Element 'Xem vị trí' found!");
                    break;
                }
            } catch (WebDriverException e) {
                System.out.println("Target element not found, going back...");
                try {
                    WebElement backButton = wait.until(ExpectedConditions.presenceOfElementLocated(backButtonLocator));
                    backButton.click();
                } catch (Exception timeoutEx) {
                    System.out.println("Back button not found after 5 seconds, exiting loop.");
                    break;
                }
                retries++;
            }
        }

        if (retries >= maxRetries) {
            System.out.println("Max retries reached. Element not found.");
        }
    }

    // Chọn một option từ dropdown dựa trên văn bản
    public void selectOptionFromDropdown(String dropdownIdentifier, String optionText) {
        if (driver == null) {
            throw new IllegalStateException("AndroidDriver chưa được khởi tạo.");
        }

        By dropdownLocator = getLocatorByText(dropdownIdentifier);
        int maxRetries = 3;
        int retryCount = 0;
        int waitBetweenRetries = 1000;

        while (retryCount < maxRetries) {
            try {
                // Chờ và nhấp vào dropdown để mở danh sách
                waitForElementToBeVisible(dropdownLocator);
                WebElement dropdownElement = findUniqueElement(dropdownLocator);
                if (dropdownElement.isDisplayed() && dropdownElement.isEnabled()) {
                    dropdownElement.click();
                    System.out.println("Clicked on dropdown: " + dropdownIdentifier);

                    // Tìm và nhấp vào option trong danh sách dropdown
                    By optionLocator = By.xpath("//*[@text='" + optionText + "']");
                    if (!isElementPresent(optionLocator)) {
                        System.out.println("Fallback to contains for option text: " + optionText);
                        optionLocator = By.xpath("//*[contains(@text, '" + optionText + "')]");
                    }

                    waitForElementToBeVisible(optionLocator);
                    WebElement optionElement = findUniqueElement(optionLocator);
                    if (optionElement.isDisplayed() && optionElement.isEnabled()) {
                        optionElement.click();
                        System.out.println("Selected option: " + optionText + " from dropdown: " + dropdownIdentifier);
                        return;
                    } else {
                        throw new RuntimeException("Option '" + optionText + "' không thể chọn.");
                    }
                } else {
                    throw new RuntimeException("Dropdown '" + dropdownIdentifier + "' không thể nhấn.");
                }
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Không thể chọn option '" + optionText + "' từ dropdown '" + dropdownIdentifier + "' sau " + maxRetries + " lần thử: " + e.getMessage());
                }
                try {
                    Thread.sleep(waitBetweenRetries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Phương thức clickUntilElementAppearsReceiveOrder
    public void clickUntilElementAppearsReceiveOrder(String backButtonIdentifier, int maxRetries) {
        int retries = 0;
        HashSet<String> clickedElements = new HashSet<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        By backButtonLocator = getLocatorByText(backButtonIdentifier);

        while (retries < maxRetries) {
            try {
                scrollToElementMobile("Mới");
                By statusLocator = By.xpath("(//android.widget.TextView[@resource-id='com.ots.c08.vnecsgt.cbcs:id/tvStatus'])[1]");
                WebElement statusElement = driver.findElement(statusLocator);
                String elementText = statusElement.getText();

                if (!clickedElements.contains(elementText)) {
                    statusElement.click();
                    clickedElements.add(elementText);
                    System.out.println("Clicked on element with text: " + elementText);
                } else {
                    System.out.println("Element already clicked, skipping...");
                }

                if (isElementPresent(By.xpath("//*[@resource-id='com.ots.c08.vnecsgt.cbcs:id/btnSend']"))) {
                    System.out.println("Element 'Xem vị trí' found!");
                    break;
                }
            } catch (WebDriverException e) {
                System.out.println("Target element not found, going back...");
                try {
                    WebElement backButton = wait.until(ExpectedConditions.presenceOfElementLocated(backButtonLocator));
                    backButton.click();
                } catch (Exception timeoutEx) {
                    System.out.println("Back button not found after 5 seconds, exiting loop.");
                    break;
                }
                retries++;
            }
        }

        if (retries >= maxRetries) {
            System.out.println("Max retries reached. Element not found.");
        }
    }
}