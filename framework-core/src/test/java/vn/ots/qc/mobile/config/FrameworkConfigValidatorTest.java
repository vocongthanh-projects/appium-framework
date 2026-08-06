package vn.ots.qc.mobile.config;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.expectThrows;

public final class FrameworkConfigValidatorTest {
    @Test public void rejectsMissingApplicationId() {
        expectThrows(ConfigurationException.class, () -> FrameworkConfigValidator.validate(FrameworkConfig.load()));
    }

    @Test public void acceptsACompleteAndroidProfile() {
        FrameworkConfigValidator.validate(FrameworkConfig.load().withOverrides(Map.of(
                "android.app.package", "com.example.application")));
    }

    @Test public void rejectsConflictingResetPolicy() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "android.app.package", "com.example.application",
                "session.noReset", "true", "session.fullReset", "true"));
        expectThrows(ConfigurationException.class, () -> FrameworkConfigValidator.validate(config));
    }
}
