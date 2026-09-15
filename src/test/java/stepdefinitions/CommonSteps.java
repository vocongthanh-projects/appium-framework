package stepdefinitions;

import bases.FunctionUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

/**
 * CommonSteps using Ultimate 'function' architecture.
 * No lifecycle hooks needed. Safe and Clean.
 */
public class CommonSteps {

    private final FunctionUtils function = new FunctionUtils();

    @Then("Text {string} phải hiển thị")
    public void textShouldBeVisible(String text) {
        function.shouldBeVisible(text);
    }

    @Then("Text {string} không được hiển thị")
    public void textShouldNotBeVisible(String text) {
        function.shouldNotBeVisible(text);
    }

    @Then("Các text sau không được hiển thị:")
    public void multipleTextsShouldNotBeVisible(DataTable dataTable) {
        List<String> unexpectedTexts = dataTable.asList();
        for (String text : unexpectedTexts) {
            function.shouldNotBeVisible(text);
        }
    }

    @Then("Các text sau phải hiển thị:")
    public void multipleTextsShouldBeVisible(DataTable dataTable) {
        List<String> expectedTexts = dataTable.asList();
        for (String text : expectedTexts) {
            function.shouldBeVisible(text);
        }
    }

    @When("Tôi click vào nút {string}")
    public void clickButtonByText(String buttonText) {
        function.clickButton(buttonText);
    }

    @When("Tôi click vào {string}")
    public void clickElementByText(String text) {
        function.click(text);
    }

    @When("Tôi nhập {string} vào trường {string}")
    public void enterTextByLabel(String value, String label) {
        function.type(label, value);
    }

    @When("Tôi nhập {string} vào trường có placeholder {string}")
    public void enterTextByPlaceholder(String text, String placeholder) {
        function.type(placeholder, text);
    }

    @When("Tôi nhập số điện thoại ngẫu nhiên vào trường có placeholder {string}")
    public void enterRandomPhoneNumberByPlaceholder(String placeholder) {
        function.typeRandomPhone(placeholder);
    }

    @Then("Tôi chờ {double} giây")
    public void waitForSeconds(double seconds) {
        try { Thread.sleep((long) (seconds * 1000)); } catch (InterruptedException ignored) {}
    }

    @When("Tôi cuộn đến phần tử có text {string}")
    public void scrollToElement(String text) {
        function.scrollIntoView(text);
    }

    @When("Tôi cuộn xuống một lần")
    public void swipeDown() {
        function.swipeDown();
    }

    @When("Tôi đóng bàn phím")
    public void hideKeyboard() {
        function.hideKeyboard();
    }

    @When("Tôi chọn {string} từ dropdown có placeholder {string}")
    public void selectOptionByPlaceholder(String option, String placeholder) {
        function.selectFromDropdown(placeholder, option);
    }

    @Then("Phần tử {string} phải ở trạng thái bật")
    public void verifyEnabled(String element) {
        function.shouldBeEnabled(element);
    }

    @Then("Phần tử {string} phải ở trạng thái vô hiệu hóa")
    public void verifyDisabled(String element) {
        function.shouldBeDisabled(element);
    }
}
