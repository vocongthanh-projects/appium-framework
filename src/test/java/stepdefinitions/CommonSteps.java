package stepdefinitions;

import bases.FunctionUltis;
import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

public class CommonSteps {
    private final FunctionUltis functionUltis = Hooks.functionUltis;

    @When("Người dùng nhấp vào {string}")
    public void userClicksElement(String identifier) {
        functionUltis.clickElement(identifier);
    }

    @When("Người dùng nhập {string} vào trường có placeholder {string}")
    public void userInputsTextIntoElement(String text, String identifier) {
        functionUltis.clickAndInput(identifier, text);
    }

    @When("Người dùng cuộn đến phần tử có văn bản {string}")
    public void userScrollsToElement(String text) {
        functionUltis.scrollToElementMobile(text);
    }

    @When("Người dùng cuộn xuống một lần")
    public void userScrollsDownOnce() {
        functionUltis.scrollDownOnce();
    }

    @When("Người dùng cuộn xuống một đoạn nhỏ")
    public void userScrollsDownSmall() {
        functionUltis.scrollDownSmall();
    }

    @When("Người dùng đóng bàn phím")
    public void userClosesKeyboard() {
        functionUltis.closeKeyboard();
    }

    @Then("Người dùng kiểm tra văn bản {string} hiển thị")
    public void userVerifiesTextDisplayed(String text) {
        functionUltis.verifyTextDisplayed(text);
    }

    @When("Người dùng nhập số điện thoại ngẫu nhiên vào trường có placeholder {string}")
    public void userInputsRandomPhoneNumber(String identifier) {
        functionUltis.inputRandomPhoneNumber(identifier);
    }

    @When("Người dùng chọn {string} từ dropdown có placeholder {string}")
    public void userSelectsOptionFromDropdown(String optionText, String dropdownIdentifier) {
        functionUltis.selectOptionFromDropdown(dropdownIdentifier, optionText);
    }

    @Then("Người dùng kiểm tra văn bản {string} không hiển thị")
    public void userVerifiesTextNotDisplayed(String text) {
        functionUltis.verifyTextNotDisplayed(text);
    }

    @Then("Người dùng kiểm tra phần tử {string} hiển thị")
    public void userVerifiesElementDisplayed(String identifier) {
        functionUltis.isElementDisplayed(identifier);
    }

    @Then("Người dùng kiểm tra phần tử {string} bị vô hiệu hóa")
    public void userVerifiesElementDisabled(String identifier) {
        functionUltis.verifyElementDisabled(identifier);
    }

    @Then("Người dùng kiểm tra phần tử {string} được kích hoạt")
    public void userVerifiesElementEnabled(String identifier) {
        functionUltis.verifyElementEnabled(identifier);
    }

    @Then("Người dùng kiểm tra văn bản của {string} là {string}")
    public void userAssertsElementText(String identifier, String expectedText) {
        functionUltis.assertElementText(identifier, expectedText);
    }

    @Then("Người dùng kiểm tra các văn bản sau hiển thị đúng:")
    public void userVerifiesMultipleTextsDisplayed(List<String> texts) {
        functionUltis.verifyMultipleTextsDisplayed(texts);
    }


}
