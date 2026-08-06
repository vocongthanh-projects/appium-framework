package vn.ots.qc.mobile.locator;

import vn.ots.qc.mobile.config.Platform;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public final class LocatorSetTest {
    @Test
    public void choosesLocatorListForRequestedPlatform() {
        By android = By.id("android-id");
        By ios = By.id("ios-id");
        LocatorSet set = new LocatorSet(List.of(android), List.of(ios));

        assertEquals(set.forPlatform(Platform.ANDROID), List.of(android));
        assertEquals(set.forPlatform(Platform.IOS), List.of(ios));
    }

    @Test
    public void rejectsLocatorSetWithoutAnySupportedPlatform() {
        expectThrows(IllegalArgumentException.class,
                () -> new LocatorSet(List.of(), List.of()));
    }

    @Test
    public void supportsExplicitPlatformOnlyLocators() {
        LocatorSet androidOnly = LocatorSet.androidOnly(By.id("android-id"));
        LocatorSet iosOnly = LocatorSet.iosOnly(By.id("ios-id"));

        assertEquals(androidOnly.forPlatform(Platform.ANDROID), List.of(By.id("android-id")));
        assertEquals(iosOnly.forPlatform(Platform.IOS), List.of(By.id("ios-id")));
        expectThrows(UnsupportedOperationException.class, () -> androidOnly.forPlatform(Platform.IOS));
        expectThrows(UnsupportedOperationException.class, () -> iosOnly.forPlatform(Platform.ANDROID));
    }

    @Test
    public void escapesNativeSelectorValues() {
        assertEquals(MobileLocators.escapeAndroid("say \"hi\""), "say \\\"hi\\\"");
        assertEquals(MobileLocators.escapeIos("it's"), "it\\'s");
    }
}
