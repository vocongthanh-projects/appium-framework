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

    @Then("Text {string} should be visible")
    public void textShouldBeVisible(String text) {
        function.shouldBeVisible(text);
    }

    @Then("Text {string} should not be visible")
    public void textShouldNotBeVisible(String text) {
        function.shouldNotBeVisible(text);
    }

    @Then("Following texts should not be visible:")
    public void multipleTextsShouldNotBeVisible(DataTable dataTable) {
        List<String> unexpectedTexts = dataTable.asList();
        for (String text : unexpectedTexts) {
            function.shouldNotBeVisible(text);
        }
    }

    @Then("Following texts should be visible:")
    public void multipleTextsShouldBeVisible(DataTable dataTable) {
        List<String> expectedTexts = dataTable.asList();
        for (String text : expectedTexts) {
            function.shouldBeVisible(text);
        }
    }

    @When("I click button {string}")
    public void clickButtonByText(String buttonText) {
        function.clickButton(buttonText);
    }

    @When("I click {string}")
    public void clickElementByText(String text) {
        function.click(text);
    }

    @When("I enter {string} into field {string}")
    public void enterTextByLabel(String value, String label) {
        function.type(label, value);
    }

    @When("I enter {string} into field with placeholder {string}")
    public void enterTextByPlaceholder(String text, String placeholder) {
        function.type(placeholder, text);
    }

    @When("I enter a random phone number into field with placeholder {string}")
    public void enterRandomPhoneNumberByPlaceholder(String placeholder) {
        function.typeRandomPhone(placeholder);
    }

    @Then("I wait for {double} seconds")
    public void waitForSeconds(double seconds) {
        try { Thread.sleep((long) (seconds * 1000)); } catch (InterruptedException ignored) {}
    }

    @When("I scroll to element with text {string}")
    public void scrollToElement(String text) {
        function.scrollIntoView(text);
    }

    @When("I scroll down once")
    public void swipeDown() {
        function.swipeDown();
    }

    @When("I close the keyboard")
    public void hideKeyboard() {
        function.hideKeyboard();
    }

    @When("I select {string} from dropdown with placeholder {string}")
    public void selectOptionByPlaceholder(String option, String placeholder) {
        function.selectFromDropdown(placeholder, option);
    }

    @Then("Element {string} should be enabled")
    public void verifyEnabled(String element) {
        function.shouldBeEnabled(element);
    }

    @Then("Element {string} should be disabled")
    public void verifyDisabled(String element) {
        function.shouldBeDisabled(element);
    }
}
