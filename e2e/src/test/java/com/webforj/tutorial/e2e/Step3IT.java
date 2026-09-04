package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class Step3IT extends BaseTest {
  @Override
  protected String stepDirectory() {
    return "3-routing-and-composites";
  }

  @Test
  void createsCustomerAndReturnsToTable() throws IOException {
    openApplication();
    expectCustomerTable();

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
        .setName("Add Customer")).click();
    assertThat(page).hasURL(Pattern.compile("/customer$"));
    assertThat(page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Submit"))).isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Cancel"))).isVisible();

    page.getByLabel("First Name").fill("Ada");
    page.getByLabel("Last Name").fill("Lovelace");
    page.getByLabel("Company").fill("Analytical Engines");
    VisualAssertions.assertScreenshot(page, "completed-customer-form.png");

    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Submit")).click();
    assertThat(page).hasURL(Pattern.compile("/$"));
    Locator adaRow = expectCustomerRow("Ada");
    assertThat(adaRow).containsText("Analytical Engines");

    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Add Customer")).click();
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Cancel")).click();
    assertThat(page).hasURL(Pattern.compile("/$"));
  }
}
