package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class Step6IT extends BaseTest {
  @Override
  protected String stepDirectory() {
    return "6-integrating-an-app-layout";
  }

  @Test
  void providesApplicationLayoutNavigation() throws IOException {
    openApplication();
    expectCustomerTable();
    assertThat(page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Customer Table").setLevel(1))).isVisible();
    VisualAssertions.assertScreenshot(page, "dashboard-layout.png");

    page.getByText("About", new Page.GetByTextOptions().setExact(true)).click();
    assertThat(page).hasURL(Pattern.compile("/about$"));
    assertThat(page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("About").setLevel(1))).isVisible();
    assertThat(page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Customer Manager").setLevel(2)).last()).isVisible();

    page.getByText("Dashboard", new Page.GetByTextOptions().setExact(true)).click();
    assertThat(page).hasURL(Pattern.compile("/$"));
    assertThat(customerTable()).isVisible();

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Customer")).click();
    assertThat(page).hasURL(Pattern.compile("/customer$"));
    assertThat(page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Customer Form").setLevel(1))).isVisible();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")).click();
    assertThat(page).hasURL(Pattern.compile("/$"));
  }

  @Test
  void keepsApplicationLayoutUsableAtNarrowViewport() throws IOException {
    page.setViewportSize(390, 844);
    openApplication();
    expectCustomerTable();

    assertThat(page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Customer Table").setLevel(1))).isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Add Customer"))).isVisible();

    Locator dashboardLink = page.getByRole(AriaRole.LINK,
        new Page.GetByRoleOptions().setName(Pattern.compile("Dashboard")));
    assertThat(dashboardLink).not().isInViewport();
    waitForLayoutToSettle();
    VisualAssertions.assertScreenshot(page, "dashboard-mobile.png");

    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName(Pattern.compile("menu", Pattern.CASE_INSENSITIVE))).click();
    assertThat(dashboardLink).isInViewport();
  }

  private void waitForLayoutToSettle() {
    page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => "
        + "requestAnimationFrame(() => resolve())))");
  }
}
