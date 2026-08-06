package vn.ots.qc.mobile.interaction;

import vn.ots.qc.mobile.config.FrameworkConfig;
import vn.ots.qc.mobile.locator.LocatorSet;
import vn.ots.qc.mobile.locator.LocatorDefinition;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ElementActions {
    private static final Logger LOG = LoggerFactory.getLogger(ElementActions.class);
    private final AppiumDriver driver;
    private final FrameworkConfig config;

    public ElementActions(AppiumDriver driver, FrameworkConfig config) {
        this.driver = driver;
        this.config = config;
    }

    public WebElement visible(LocatorSet locators) {
        return visible(locators, config.explicitWait());
    }

    public WebElement visible(LocatorDefinition locator) {
        LOG.debug("Resolve mobile locator '{}'", locator.logicalName());
        return visible(locator.locators());
    }

    public WebElement visible(LocatorSet locators, Duration timeout) {
        List<By> candidates = locators.forPlatform(config.platform());
        return waitFor(timeout).until(ignored -> firstDisplayed(candidates).orElse(null));
    }

    public boolean isVisible(LocatorSet locators, Duration timeout) {
        try {
            visible(locators, timeout);
            return true;
        } catch (TimeoutException | NoSuchElementException exception) {
            return false;
        }
    }

    public void click(LocatorSet locators) {
        waitFor(config.explicitWait()).until(ignored -> {
            Optional<WebElement> element = firstDisplayed(locators.forPlatform(config.platform()));
            if (element.isEmpty() || !element.get().isEnabled()) {
                return false;
            }
            try {
                element.get().click();
                return true;
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });
    }

    public void click(LocatorDefinition locator) {
        LOG.debug("Click mobile locator '{}'", locator.logicalName());
        click(locator.locators());
    }

    public void type(LocatorSet locators, String value) {
        WebElement element = visible(locators);
        element.clear();
        element.sendKeys(value);
    }

    public void type(LocatorDefinition locator, String value) {
        LOG.debug("Type into mobile locator '{}'", locator.logicalName());
        type(locator.locators(), value);
    }

    public void typeSensitive(LocatorDefinition locator, String value) {
        LOG.debug("Type sensitive value into mobile locator '{}'", locator.logicalName());
        type(locator.locators(), value);
    }

    public String textOf(LocatorSet locators) {
        return visible(locators).getText();
    }

    public String textOf(LocatorDefinition locator) { return textOf(locator.locators()); }
    public String attributeOf(LocatorDefinition locator, String attribute) {
        if (attribute == null || attribute.isBlank()) throw new IllegalArgumentException("attribute is required");
        return visible(locator).getAttribute(attribute);
    }

    public void clear(LocatorDefinition locator) {
        LOG.debug("Clear mobile locator '{}'", locator.logicalName());
        visible(locator).clear();
    }

    public boolean isEnabled(LocatorDefinition locator) { return visible(locator).isEnabled(); }
    public boolean isSelected(LocatorDefinition locator) { return visible(locator).isSelected(); }

    public void shouldBeEnabled(LocatorDefinition locator) {
        if (!isEnabled(locator)) throw new AssertionError("Element is disabled: " + locator.logicalName());
    }

    public void shouldBeDisabled(LocatorDefinition locator) {
        if (isEnabled(locator)) throw new AssertionError("Element is enabled: " + locator.logicalName());
    }

    public void shouldHaveText(LocatorDefinition locator, String expected) {
        String actual = textOf(locator);
        if (!java.util.Objects.equals(actual, expected)) {
            throw new AssertionError("Unexpected text for " + locator.logicalName()
                    + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
    public void shouldBeVisible(LocatorDefinition locator) { shouldBeVisible(locator.locators()); }
    public void shouldNotBeVisible(LocatorDefinition locator) { shouldNotBeVisible(locator.locators()); }

    public void shouldBeVisible(LocatorSet locators) {
        visible(locators);
    }

    public void shouldNotBeVisible(LocatorSet locators) {
        if (isVisible(locators, Duration.ofSeconds(2))) {
            throw new AssertionError("Element is still visible: " + locators.forPlatform(config.platform()));
        }
    }

    public void waitUntilGone(LocatorDefinition locator) {
        waitFor(config.explicitWait()).until(ignored -> !isVisible(locator.locators(), config.pollingInterval()));
    }

    public void hideKeyboard() {
        try {
            if (driver instanceof HasOnScreenKeyboard keyboard
                    && driver instanceof HidesKeyboard keyboardActions
                    && keyboard.isKeyboardShown()) {
                keyboardActions.hideKeyboard();
            }
        } catch (RuntimeException ignored) {
            // Some drivers do not implement keyboard state consistently.
        }
    }

    private Optional<WebElement> firstDisplayed(List<By> candidates) {
        for (By locator : candidates) {
            for (WebElement element : driver.findElements(locator)) {
                try {
                    if (element.isDisplayed()) {
                        return Optional.of(element);
                    }
                } catch (StaleElementReferenceException ignored) {
                    // Continue with the next candidate during this poll.
                }
            }
        }
        return Optional.empty();
    }

    private Wait<AppiumDriver> waitFor(Duration timeout) {
        return new FluentWait<>(driver)
                .withTimeout(timeout)
                .pollingEvery(config.pollingInterval())
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }
}
