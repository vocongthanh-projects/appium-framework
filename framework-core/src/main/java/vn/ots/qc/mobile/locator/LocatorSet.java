package vn.ots.qc.mobile.locator;

import vn.ots.qc.mobile.config.Platform;
import org.openqa.selenium.By;

import java.util.List;

/** Platform-specific locator alternatives ordered from most to least stable. */
public record LocatorSet(List<By> android, List<By> ios) {
    public LocatorSet {
        android = List.copyOf(android);
        ios = List.copyOf(ios);
        if (android.isEmpty() && ios.isEmpty()) {
            throw new IllegalArgumentException("At least one platform locator list is required");
        }
    }

    public static LocatorSet same(By locator) {
        return new LocatorSet(List.of(locator), List.of(locator));
    }

    public static LocatorSet androidOnly(By... locators) {
        return new LocatorSet(List.of(locators), List.of());
    }

    public static LocatorSet iosOnly(By... locators) {
        return new LocatorSet(List.of(), List.of(locators));
    }

    public List<By> forPlatform(Platform platform) {
        List<By> selected = platform == Platform.ANDROID ? android : ios;
        if (selected.isEmpty()) {
            throw new UnsupportedOperationException("This locator set does not support " + platform);
        }
        return selected;
    }
}
