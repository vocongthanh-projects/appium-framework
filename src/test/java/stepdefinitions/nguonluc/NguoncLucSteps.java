package stepdefinitions.nguonluc;

import bases.FunctionUltis;
import hooks.Hooks;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;

public class NguoncLucSteps {
    private final FunctionUltis functionUltis = Hooks.functionUltis;

    @Given("Người dùng nhấn vào filter Nguồn lực")
    public void nhanVaoMenuNguonLuc() {
        functionUltis.clickElementByLocator(By.id("com.ots.gsafe.firefighter:id/imvFilter"));
    }

}
