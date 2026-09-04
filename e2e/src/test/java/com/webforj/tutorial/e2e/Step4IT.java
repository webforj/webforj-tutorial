package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class Step4IT extends BaseTest {
  @Override
  protected String stepDirectory() {
    return "4-observers-and-route-parameters";
  }

  @Test
  void showsRoutedCustomerFormConsistently() throws IOException {
    openApplication("/customer/1");
    assertThat(page.getByLabel("First Name")).hasValue("Alice");
    assertThat(page.getByLabel("Last Name")).hasValue("Smith");
    assertThat(page.getByLabel("Company")).hasValue("TechCorp");
    VisualAssertions.assertScreenshot(page, "edit-customer-route.png");
  }

  @Test
  void loadsCustomerFromRouteAndSavesEdits() {
    openApplication();
    expectCustomerTable();

    page.getByText("John", new Page.GetByTextOptions().setExact(true)).click();
    assertThat(page).hasURL(Pattern.compile("/customer/2$"));
    assertThat(page.getByLabel("First Name")).hasValue("John");
    assertThat(page.getByLabel("Last Name")).hasValue("Doe");

    var company = page.getByLabel("Company");
    company.click();
    company.press("Control+A");
    company.pressSequentially("Updated Innovatech");
    assertThat(company).hasValue("Updated Innovatech");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
    assertThat(page).hasURL(Pattern.compile("/$"));
    assertThat(expectCustomerRow("John")).containsText("Updated Innovatech");

    openApplication("/customer/999999");
    assertThat(page).hasURL(Pattern.compile("/$"));
    assertThat(customerTable()).isVisible();
  }
}
