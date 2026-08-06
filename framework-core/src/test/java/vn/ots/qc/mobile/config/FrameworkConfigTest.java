package vn.ots.qc.mobile.config;

import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public final class FrameworkConfigTest {
    @Test
    public void resolvesSystemPropertyBeforeEnvironmentAndFile() throws Exception {
        Path override = Files.createTempFile("mobile-config-", ".properties");
        Files.writeString(override, "platform=ios\ntimeout.explicit.seconds=40\n");

        Properties system = new Properties();
        system.setProperty("config.file", override.toString());
        system.setProperty("platform", "android");
        FrameworkConfig config = FrameworkConfig.load(system, Map.of("PLATFORM", "ios"));

        assertEquals(config.platform(), Platform.ANDROID);
        assertEquals(config.duration("timeout.explicit.seconds").toSeconds(), 40L);
        Files.deleteIfExists(override);
    }

    @Test
    public void resolvesEnvironmentBeforeFileAndDefaults() throws Exception {
        Path override = Files.createTempFile("mobile-config-", ".properties");
        Files.writeString(override, "platform=android\n");
        Properties system = new Properties();
        system.setProperty("config.file", override.toString());

        FrameworkConfig config = FrameworkConfig.load(system, Map.of("PLATFORM", "ios"));

        assertEquals(config.platform(), Platform.IOS);
        Files.deleteIfExists(override);
    }

    @Test
    public void mapsCamelCaseSettingsToReadableEnvironmentNames() {
        FrameworkConfig config = FrameworkConfig.load(new Properties(), Map.of("ANDROID_SYSTEM_PORT", "8299"));

        assertEquals(config.getInt("android.systemPort"), 8299);
    }

    @Test
    public void rejectsInvalidBooleanInsteadOfSilentlyUsingFalse() {
        Properties system = new Properties();
        system.setProperty("session.noReset", "sometimes");
        FrameworkConfig config = FrameworkConfig.load(system, Map.of());

        expectThrows(ConfigurationException.class, () -> config.getBoolean("session.noReset"));
    }

    @Test
    public void rejectsUnknownPlatform() {
        Properties system = new Properties();
        system.setProperty("platform", "windows-phone");
        FrameworkConfig config = FrameworkConfig.load(system, Map.of());

        expectThrows(ConfigurationException.class, config::platform);
    }
}
