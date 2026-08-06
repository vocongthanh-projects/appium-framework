package vn.ots.qc.mobile.evidence;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.ots.qc.mobile.config.FrameworkConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class MobileEvidenceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(MobileEvidenceCollector.class);
    private final FrameworkConfig config;
    private final PageSourceRedactor redactor = new PageSourceRedactor();

    public MobileEvidenceCollector(FrameworkConfig config) { this.config = config; }

    public MobileEvidence capture(AppiumDriver driver, String testName) {
        Path directory = Path.of(config.required("evidence.directory")).toAbsolutePath().normalize();
        String safe = safe(testName) + "-" + Instant.now().toEpochMilli();
        Path screenshot = directory.resolve(safe + ".png");
        Path source = directory.resolve(safe + ".xml");
        try {
            Files.createDirectories(directory);
            Files.write(screenshot, driver.getScreenshotAs(OutputType.BYTES));
        } catch (Exception exception) {
            LOG.warn("Could not capture mobile screenshot ({})", exception.getClass().getSimpleName());
            screenshot = null;
        }
        try {
            Files.createDirectories(directory);
            Files.writeString(source, redactor.redact(driver.getPageSource()), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            LOG.warn("Could not capture redacted mobile page source ({})", exception.getClass().getSimpleName());
            source = null;
        }
        return new MobileEvidence(screenshot, source);
    }

    private String safe(String value) {
        String safe = value == null ? "unnamed-test" : value.toLowerCase().replaceAll("[^a-z0-9._-]+", "-");
        return safe.replaceAll("(^-+|-+$)", "");
    }
}
