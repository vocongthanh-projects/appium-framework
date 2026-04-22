package pages;

import bases.ConfigManager;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Modernized LoginPage - Zero raw Selenium methods. 
 * Interaction is entirely handled by FunctionUtils with built-in auto-wait.
 */
public class LoginPage extends BasePage {

    // --- Selectors - Standard ui_gtel_map style ---
    private static final String PKG = ConfigManager.getAppPackage();
    private static final By USERNAME_INPUT  = By.id(PKG + ":id/edtPhone");
    private static final By PASSWORD_INPUT  = By.id(PKG + ":id/edtPassword");
    private static final By LOGIN_BUTTON    = By.id(PKG + ":id/btnLogin");
    private static final By DASHBOARD_INDICATOR = By.id(PKG + ":id/navigation_home");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    // --- Actions (Strictly through FunctionUtils) ---

    public void enterUsername(String user) {
        util.typeElement(USERNAME_INPUT, user);
    }

    public void enterPassword(String pass) {
        util.typeElement(PASSWORD_INPUT, pass);
    }

    public void clickLogin() {
        util.clickElement(LOGIN_BUTTON);
    }

    /**
     * Intelligent login flow with smart session bypass.
     * Replaced raw Selenium findElement with robust util visibility checks.
     */
    public void login() {
        // Fast bypass check: wait up to 5 seconds
        if (isAlreadyLoggedIn()) {
            System.out.println("⚡ Session already active, bypassing login screen.");
            return;
        }

        String user = ConfigManager.getUsername();
        String pass = ConfigManager.getPassword();
        
        System.out.println("🔐 Authenticating user: " + user);
        enterUsername(user);
        enterPassword(pass);
        clickLogin();

        // Final verification after login: use full timeout
        if (!isAlreadyLoggedInFullTimeout()) {
            throw new RuntimeException("❌ Post-login verification failed: Dashboard not visible.");
        }
        System.out.println("✅ Login successful.");
    }

    /**
     * Quick visibility check with a short wait (for bypass session).
     */
    public boolean isAlreadyLoggedIn() {
        return util.isVisible(DASHBOARD_INDICATOR, 5); 
    }

    /**
     * In-depth visibility check using default timeout (for login confirmation).
     */
    private boolean isAlreadyLoggedInFullTimeout() {
        return util.isVisible(DASHBOARD_INDICATOR);
    }
}
