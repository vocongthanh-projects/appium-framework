package vn.ots.qc.mobile.locator;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public final class MobileLocatorTest {
    @Test public void bindsLogicalNameToPlatformFallbacks() {
        MobileLocator locator = MobileLocator.of("account.email", LocatorSet.same(By.id("email")));
        assertEquals(locator.logicalName(), "account.email");
        assertEquals(locator.locators().android(), java.util.List.of(By.id("email")));
    }

    @Test public void rejectsBlankLogicalName() {
        expectThrows(IllegalArgumentException.class,
                () -> MobileLocator.of(" ", LocatorSet.same(By.id("email"))));
    }
}
