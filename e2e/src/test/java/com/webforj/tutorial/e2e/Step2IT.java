package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class Step2IT extends BaseTest {
  @Override
  protected String stepDirectory() {
    return "2-working-with-data";
  }

  @Test
  void rendersSeededDataDialogAndSorting() throws IOException {
    openApplication();

    assertThat(page.getByText("Tutorial App!", new Page.GetByTextOptions().setExact(true))).isVisible();
    Locator table = expectCustomerTable();
    assertThat(table).containsText("John");
    assertThat(table).containsText("Innovatech");

    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Info").setExact(true)).click();
    assertThat(page.getByText("This is a tutorial!", new Page.GetByTextOptions().setExact(true))).isVisible();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("OK")).click();
    assertThat(page.getByRole(AriaRole.DIALOG)).isHidden();
    VisualAssertions.assertScreenshot(page, "customer-table.png");

    Locator firstNameHeader = page.locator(
        "dwc-table [part~='cell-header'][data-column='firstName']");
    firstNameHeader.click();
    waitForRowOrder("Emma", "John");
    firstNameHeader.click();
    waitForRowOrder("John", "Emma");
  }

  private void waitForRowOrder(String first, String second) {
    long deadline = System.nanoTime() + 10_000_000_000L;
    while (System.nanoTime() < deadline) {
      if (rowTop(first) < rowTop(second)) {
        return;
      }
      page.waitForTimeout(100);
    }
    fail("Expected row " + first + " to appear above " + second);
  }

  private double rowTop(String name) {
    Locator cell = page.getByText(name, new Page.GetByTextOptions().setExact(true));
    BoundingBox box = cell.boundingBox();
    return box == null ? Double.MAX_VALUE : box.y;
  }
}
