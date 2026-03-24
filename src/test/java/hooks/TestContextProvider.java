package hooks;

import io.appium.java_client.android.AndroidDriver;
import lombok.Getter;

import java.util.Objects;

/**
 * Standard Context Provider matching ui_gtel_map exactly.
 * Wraps the ThreadLocal driver for access in Step Definitions.
 */
@Getter
public class TestContextProvider {
    private static final ThreadLocal<TestContextProvider> instanceThread = new ThreadLocal<>();
    /**
     * -- GETTER --
     *  Manual getter to avoid Lombok configuration issues in Maven compiler.
     */
    private final AndroidDriver driver;

    private TestContextProvider(AndroidDriver driver) {
        this.driver = driver;
    }

    public static void setContext(AndroidDriver driver) {
        instanceThread.set(new TestContextProvider(driver));
    }

    public static TestContextProvider getInstance() {
        return Objects.requireNonNull(instanceThread.get(), "❌ TestContextProvider not initialized!");
    }

    /* Alias for compatibility with existing code */
    public static TestContextProvider getContext() {
        return getInstance();
    }

    public static void clear() {
        instanceThread.remove();
    }

}
