package stepdefinitions.danhsachden;

import bases.FunctionUltis;
import hooks.Hooks;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;

public class DanhSachDenSteps {
    private final FunctionUltis functionUltis = Hooks.functionUltis;

    @Given("Người dùng nhấn vào menu Danh sách đen")
    public void nhanVaoMenuDanhSachDen() {
        functionUltis.clickElementByLocator(By.id("com.ots.c08.vnecsgt.cbcs:id/ivMenu"));
        functionUltis.clickElementByLocator(By.xpath("//android.widget.TextView[@text='Danh sách đen']"));
    }

    @Given("Người dùng nhấn vào nút thêm mới danh sách đen")
    public void nhanVaoMenuQuanLyDoiTuong() {
        functionUltis.clickElementByLocator(By.id("com.ots.c08.vnecsgt.cbcs:id/btnAdd"));
    }


}
