package pages;

import bases.FunctionUtils;
import io.appium.java_client.android.AndroidDriver;

import java.util.Objects;

/**
 * Enhanced BasePage for flexible driver initialization.
 * Zero raw Selenium dependency in initialization.
 */
public abstract class BasePage {
    protected AndroidDriver driver;
    protected FunctionUtils util;

    protected BasePage(AndroidDriver driver) {
        if (driver != null) {
            initElements(driver);
        }
    }

    /**
     * Re-synchronized to use the new no-arg FunctionUtils constructor.
     */
    protected void initElements(AndroidDriver driver) {
        this.driver = driver;
        this.util = new FunctionUtils(); 
    }
}
