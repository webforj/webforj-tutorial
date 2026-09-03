package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class Step4IT extends BaseTest {
  @Override
  protected String stepDirectory() {
    return "4-observers-and-route-parameters";
  }

  @Test
  @Order(1)
  void showsRoutedCustomerFormConsistently() throws IOException {
    openApplication("/customer/1");
    assertThat(page.getByLabel("First Name")).hasValue("Alice");
    assertThat(page.getByLabel("Last Name")).hasValue("Smith");
    assertThat(page.getByLabel("Company")).hasValue("TechCorp");
    VisualAssertions.assertScreenshot(page, "edit-customer-route.png");
  }

  @Test
  @Order(2)
  void loadsCustomerFromRouteAndSavesEdits() {
    openApplication();
    expectCustomerTable();

    page.getByText("Alice", new Page.GetByTextOptions().setExact(true)).click();
    assertThat(page).hasURL(Pattern.compile("/customer/1$"));
    assertThat(page.getByLabel("First Name")).hasValue("Alice");
    assertThat(page.getByLabel("Last Name")).hasValue("Smith");

    var company = page.getByLabel("Company");
    company.click();
    company.press("Control+A");
    company.pressSequentially("Updated TechCorp", new com.microsoft.playwright.Locator.PressSequentiallyOptions()
        .setDelay(25));
    assertThat(company).hasValue("Updated TechCorp");
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
    assertThat(page).hasURL(Pattern.compile("/$"));
    assertThat(page.getByRole(AriaRole.CELL,
        new Page.GetByRoleOptions().setName("Updated TechCorp").setExact(true))).isVisible();

    openApplication("/customer/999999");
    assertThat(page).hasURL(Pattern.compile("/$"));
    assertThat(customerTable()).isVisible();
  }
}
