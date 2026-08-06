package vn.ots.qc.mobile.evidence;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public final class PageSourceRedactorTest {
    @Test public void redactsAndroidAndIosSecureFields() {
        String source = "<root><node password=\"true\" text=\"secret-one\"/>"
                + "<node type=\"XCUIElementTypeSecureTextField\" value=\"secret-two\"/></root>";
        String redacted = new PageSourceRedactor().redact(source);
        assertFalse(redacted.contains("secret-one"));
        assertFalse(redacted.contains("secret-two"));
        assertTrue(redacted.contains("redacted"));
    }

    @Test public void disablesDoctypeAndStillRedactsFallbackText() {
        String source = "<!DOCTYPE root [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]><root password=secret/>";
        String redacted = new PageSourceRedactor().redact(source);
        assertFalse(redacted.contains("password=secret"));
    }
}
