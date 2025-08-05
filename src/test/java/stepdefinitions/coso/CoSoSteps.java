package stepdefinitions.coso;

import bases.FunctionUltis;
import hooks.Hooks;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;

public class CoSoSteps {
    private final FunctionUltis functionUltis = Hooks.functionUltis;

    @Given("Người dùng nhấn vào thêm mới cơ sở")
    public void nhanVaoMenuNguonLuc() {
        functionUltis.clickElementByLocator(By.id("com.ots.gsafe.firefighter:id/btnAdd"));
    }

    @Given("Người dùng nhấn vào Xem vị trí trên bản đồ")
    public void nhanVaoXemViTri() {
        functionUltis.clickElementByLocator(By.xpath("(//android.widget.Button[@resource-id=\"com.ots.gsafe.firefighter:id/btnMap\"])[1]"));
    }

}
