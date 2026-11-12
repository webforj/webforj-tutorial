package com.webforj.tutorial;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class Step1IT extends BaseTest {
  @Test
  void rendersBasicApplicationAndOpensInformationDialog() throws IOException {
    openApplication();

    assertThat(page.getByText("Tutorial App!", new Page.GetByTextOptions().setExact(true))).isVisible();
    var infoButton = page.getByRole(
        AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Info").setExact(true));
    assertThat(infoButton).isVisible();
    infoButton.click();

    assertThat(page.getByRole(AriaRole.DIALOG)).isVisible();
    assertThat(page.getByText("This is a tutorial!", new Page.GetByTextOptions().setExact(true))).isVisible();
    VisualAssertions.assertScreenshot(page, "basic-application-dialog.png");
  }
}
