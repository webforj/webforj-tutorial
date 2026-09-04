package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
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
    expectCustomerTable();
    assertThat(expectCustomerRow("John")).containsText("Innovatech");

    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Info").setExact(true)).click();
    assertThat(page.getByText("This is a tutorial!", new Page.GetByTextOptions().setExact(true))).isVisible();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("OK")).click();
    assertThat(page.getByRole(AriaRole.DIALOG)).isHidden();
    VisualAssertions.assertScreenshot(page, "customer-table.png");

    Locator firstNameHeader = page.locator(
        "dwc-table [part~='cell-header'][data-column='firstName']");
    Locator firstNameCells = page.locator(
        "dwc-table [part~='cell'][data-column='firstName']:not([part~='cell-header'])");

    firstNameHeader.click();
    assertThat(firstNameCells).hasText(new String[] {
        "Alice", "Emma", "Isabella", "James", "John",
        "Liam", "Lucas", "Noah", "Olivia", "Sophia"
    });

    firstNameHeader.click();
    assertThat(firstNameCells).hasText(new String[] {
        "Sophia", "Olivia", "Noah", "Lucas", "Liam",
        "John", "James", "Isabella", "Emma", "Alice"
    });
  }
}
