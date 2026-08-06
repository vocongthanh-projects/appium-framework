package vn.ots.qc.mobile.interaction;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

public final class Gestures {
    private final AppiumDriver driver;

    public Gestures(AppiumDriver driver) {
        this.driver = driver;
    }

    public void swipeUp() {
        swipe(0.75, 0.30, Duration.ofMillis(550));
    }

    public void swipeDown() {
        swipe(0.30, 0.75, Duration.ofMillis(550));
    }

    public void swipeLeft() { horizontalSwipe(0.80, 0.20, Duration.ofMillis(550)); }
    public void swipeRight() { horizontalSwipe(0.20, 0.80, Duration.ofMillis(550)); }

    public void swipe(double startYRatio, double endYRatio, Duration duration) {
        if (startYRatio < 0 || startYRatio > 1 || endYRatio < 0 || endYRatio > 1) {
            throw new IllegalArgumentException("Swipe ratios must be between 0 and 1");
        }
        Dimension size = driver.manage().window().getSize();
        int x = size.width / 2;
        int startY = (int) (size.height * startYRatio);
        int endY = (int) (size.height * endYRatio);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0);
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, startY));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(new Pause(finger, Duration.ofMillis(100)));
        sequence.addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(), x, endY));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    public void horizontalSwipe(double startXRatio, double endXRatio, Duration duration) {
        ratio(startXRatio); ratio(endXRatio);
        Dimension size = driver.manage().window().getSize();
        perform((int) (size.width * startXRatio), size.height / 2,
                (int) (size.width * endXRatio), size.height / 2, duration, Duration.ofMillis(100));
    }

    public void tap(WebElement element) { press(element, Duration.ofMillis(80)); }

    public void longPress(WebElement element, Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Long press duration must be positive");
        }
        press(element, duration);
    }

    private void press(WebElement element, Duration hold) {
        org.openqa.selenium.Rectangle rectangle = java.util.Objects.requireNonNull(element).getRect();
        int x = rectangle.x + rectangle.width / 2;
        int y = rectangle.y + rectangle.height / 2;
        perform(x, y, x, y, Duration.ZERO, hold);
    }

    private void perform(int startX, int startY, int endX, int endY, Duration move, Duration hold) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0);
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(new Pause(finger, hold));
        sequence.addAction(finger.createPointerMove(move, PointerInput.Origin.viewport(), endX, endY));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    private static void ratio(double value) {
        if (value < 0 || value > 1) throw new IllegalArgumentException("Swipe ratios must be between 0 and 1");
    }
}
