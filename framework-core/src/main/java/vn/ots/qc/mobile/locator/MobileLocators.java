package vn.ots.qc.mobile.locator;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.List;

public final class MobileLocators {
    private MobileLocators() {
    }

    public static LocatorSet accessibilityId(String id) {
        return LocatorSet.same(AppiumBy.accessibilityId(id));
    }

    public static LocatorSet accessibilityIdOrText(String id, String text) {
        LocatorSet textLocators = text(text);
        return new LocatorSet(
                prepend(AppiumBy.accessibilityId(id), textLocators.android()),
                prepend(AppiumBy.accessibilityId(id), textLocators.ios())
        );
    }

    /** Generic fallback for legacy screens; screen objects should prefer stable IDs. */
    public static LocatorSet text(String text) {
        String android = escapeAndroid(text);
        String ios = escapeIos(text);
        return new LocatorSet(
                List.of(
                        AppiumBy.androidUIAutomator("new UiSelector().text(\"" + android + "\")"),
                        AppiumBy.androidUIAutomator("new UiSelector().description(\"" + android + "\")")
                ),
                List.of(
                        AppiumBy.iOSNsPredicateString("label == '" + ios + "' OR name == '" + ios + "' OR value == '" + ios + "'"),
                        AppiumBy.accessibilityId(text)
                )
        );
    }

    public static LocatorSet input(String accessibilityId, String visibleLabel) {
        String android = escapeAndroid(visibleLabel);
        String ios = escapeIos(visibleLabel);
        return new LocatorSet(
                List.of(
                        AppiumBy.accessibilityId(accessibilityId),
                        AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").textContains(\"" + android + "\")")
                ),
                List.of(
                        AppiumBy.accessibilityId(accessibilityId),
                        AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeTextField' AND (label CONTAINS '" + ios + "' OR value CONTAINS '" + ios + "')"),
                        AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeSecureTextField' AND (label CONTAINS '" + ios + "' OR value CONTAINS '" + ios + "')")
                )
        );
    }

    static String escapeAndroid(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String escapeIos(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static List<By> prepend(By first, List<By> rest) {
        java.util.ArrayList<By> locators = new java.util.ArrayList<>(rest.size() + 1);
        locators.add(first);
        locators.addAll(rest);
        return List.copyOf(locators);
    }
}
