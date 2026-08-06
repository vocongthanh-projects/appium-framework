package vn.ots.qc.mobile.locator;

import java.util.Objects;

/** Application-owned logical name plus ordered Android/iOS candidates. */
public record MobileLocator(String logicalName, LocatorSet locators) implements LocatorDefinition {
    public MobileLocator {
        if (logicalName == null || logicalName.isBlank()) {
            throw new IllegalArgumentException("Logical locator name is required");
        }
        logicalName = logicalName.trim();
        Objects.requireNonNull(locators, "locators");
    }

    public static MobileLocator of(String logicalName, LocatorSet locators) {
        return new MobileLocator(logicalName, locators);
    }
}
