package bases;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Native Locator Manager for Android Automation (Replaced XPath).
 * Uses AppiumBy.androidUIAutomator(UiSelector) for lightning-fast native execution speeds.
 */
public class XpathList {

    public static List<By> getButtonLocators(String label) {
        return List.of(
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").textContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Button\").descriptionContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.TextView\").textContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().clickable(true).textContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().clickable(true).descriptionContains(\"" + label + "\")")
        );
    }

    public static List<By> getInputLocators(String label) {
        return List.of(
            // 1. Ưu tiên tìm TRỰC TIẾP theo placeholder/hint nằm trong chính ô EditText (Chính xác nhất)
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").textContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").descriptionContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").resourceIdMatches(\".*" + label + ".*\")"),
            
            // 2. Fallback: Tìm theo label đứng cạnh (Dùng trong TextInputLayout)
            // Nhắm mục tiêu cụ thể hơn bằng cách kết hợp với gợi ý text
            AppiumBy.androidUIAutomator("new UiSelector().text(\"" + label + "\").fromParent(new UiSelector().className(\"android.widget.EditText\"))"),
            AppiumBy.androidUIAutomator("new UiSelector().description(\"" + label + "\").fromParent(new UiSelector().className(\"android.widget.EditText\"))")
        );
    }


    public static List<By> getDropdownLocators(String label) {
        return List.of(
            // Tương đương: //*[contains(@text, 'label')]/following-sibling::*[@clickable='true']
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + label + "\").fromParent(new UiSelector().clickable(true))"),
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.Spinner\").textContains(\"" + label + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().resourceIdMatches(\".*spinner.*\").textContains(\"" + label + "\")")
        );
    }

    public static List<By> getPriorityLocators(String text) {
        return List.of(
            // 1. Khớp tuyệt đối (Exact Match) - Rất quan trọng cho các nút như "Có", "Không", "OK"
            AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\").clickable(true)"),
            AppiumBy.androidUIAutomator("new UiSelector().description(\"" + text + "\").clickable(true)"),
            
            // 2. Dự phòng: Khớp chứa (Contains)
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + text + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"" + text + "\")"),
            AppiumBy.androidUIAutomator("new UiSelector().resourceIdMatches(\".*" + text + ".*\")")
        );
    }

}
