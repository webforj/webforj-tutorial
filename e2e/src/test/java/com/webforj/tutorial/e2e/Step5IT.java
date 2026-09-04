package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class Step5IT extends BaseTest {
  @Override
  protected String stepDirectory() {
    return "5-validating-and-binding-data";
  }

  @Test
  void bindsAndValidatesCustomerBeforeSubmission() throws IOException {
    openApplication();
    expectCustomerTable();

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Customer")).click();
    var submit = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit"));
    assertThat(submit).isEnabled();

    page.getByLabel("First Name").fill("Grace");
    submit.click();
    assertThat(page).hasURL(Pattern.compile("/customer$"));
    var lastNameError = page.getByText(
        "Customer last name is required", new Page.GetByTextOptions().setExact(true));
    assertThat(lastNameError).isVisible();
    page.getByLabel("Last Name").fill("Hopper");
    page.getByLabel("Company").fill("Compiler Systems");
    assertThat(lastNameError).isHidden();
    assertThat(submit).isEnabled();
    VisualAssertions.assertScreenshot(page, "valid-customer-form.png");

    submit.click();
    assertThat(page).hasURL(Pattern.compile("/$"));
    assertThat(expectCustomerRow("Grace")).containsText("Compiler Systems");
  }

  @Test
  void loadsExistingCustomerIntoBindingContext() {
    openApplication("/customer/1");
    assertThat(page.getByLabel("First Name")).hasValue("Alice");
    assertThat(page.getByLabel("Last Name")).hasValue("Smith");
    assertThat(page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Submit"))).isEnabled();
  }
}
