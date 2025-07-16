package stepdefinitions.trangchu;

import bases.FunctionUltis;
import hooks.Hooks;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;

public class TrangChuSteps {
    private final FunctionUltis functionUltis = Hooks.functionUltis;

    @Given("Người dùng nhấn vào filter Nguồn lực Trang chủ")
    public void nhanVaoMenuNguonLuc() {
        functionUltis.clickElementByLocator(By.xpath("//android.widget.RelativeLayout[@resource-id=\"com.ots.gsafe.firefighter:id/rlFilter\"]/android.widget.ImageView"));
    }

    @Given("Người dùng nhấn vào thêm mới Nguồn lực")
    public void nhanVaoThemMoiNguonLuc() {
        functionUltis.clickElementByLocator(By.id("com.ots.gsafe.firefighter:id/btnAddResource"));
    }

}
