package com.webforj.tutorial.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.WaitUntilState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private TutorialApp application;
  private final List<String> browserErrors = new ArrayList<>();

  protected Page page;

  protected abstract String stepDirectory();

  @BeforeAll
  void startBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    PlaywrightAssertions.setDefaultAssertionTimeout(10_000);
  }

  @BeforeEach
  void startApplicationAndCreateBrowserContext() throws Exception {
    browserErrors.clear();
    application = TutorialApp.start(stepDirectory());
    context = browser.newContext(new Browser.NewContextOptions()
        .setViewportSize(1440, 900)
        .setDeviceScaleFactor(1)
        .setColorScheme(ColorScheme.LIGHT)
        .setLocale("en-US")
        .setTimezoneId("UTC")
        .setIgnoreHTTPSErrors(true));
    context.tracing().start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true)
        .setSources(true));

    page = context.newPage();
    page.onPageError(error -> browserErrors.add("pageerror: " + error));
    page.onConsoleMessage(this::recordConsoleError);
    page.onRequestFailed(this::recordRequestFailure);
  }

  @AfterEach
  void closeBrowserContext(TestInfo testInfo) throws IOException {
    List<String> errorsBeforeClose = List.copyOf(browserErrors);
    try {
      if (context != null) {
        Path traceDirectory = Path.of("target", "playwright-traces");
        Files.createDirectories(traceDirectory);
        String testName = (getClass().getSimpleName() + "-" + testInfo.getDisplayName())
            .replaceAll("[^a-zA-Z0-9.-]", "-");
        context.tracing().stop(new Tracing.StopOptions()
            .setPath(traceDirectory.resolve(testName + ".zip")));
        context.close();
        context = null;
      }
    } finally {
      if (application != null) {
        application.close();
        application = null;
      }
    }
    assertTrue(errorsBeforeClose.isEmpty(),
        () -> "Browser runtime/network errors:\n" + String.join("\n", errorsBeforeClose));
  }

  @AfterAll
  void stopBrowser() {
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

  protected void openApplication() {
    openApplication("/");
  }

  protected void openApplication(String path) {
    String normalizedPath = path.startsWith("/") ? path : "/" + path;
    page.navigate(application.baseUrl() + normalizedPath,
        new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
  }

  protected Locator customerTable() {
    return page.locator("dwc-table");
  }

  protected Locator expectCustomerTable() {
    Locator table = customerTable();
    assertThat(table).isVisible();
    assertThat(table).containsText("First Name");
    assertThat(table).containsText("Last Name");
    assertThat(table).containsText("Company");
    assertThat(table).containsText("Country");
    assertThat(table).containsText("Alice");
    assertThat(table).containsText("TechCorp");
    return table;
  }

  protected Locator expectCustomerRow(String identifyingCellText) {
    Locator identifyingCell = page.getByRole(AriaRole.CELL,
        new Page.GetByRoleOptions().setName(identifyingCellText).setExact(true));
    Locator row = page.getByRole(AriaRole.ROW)
        .filter(new Locator.FilterOptions().setHas(identifyingCell));
    assertThat(row).hasCount(1);
    return row;
  }

  private void recordConsoleError(ConsoleMessage message) {
    if ("error".equals(message.type())) {
      browserErrors.add("console: " + message.text());
    }
  }

  private void recordRequestFailure(Request request) {
    String failure = request.failure();
    boolean cancelledWebforjPoll = "net::ERR_ABORTED".equals(failure)
        && request.url().contains("/webforjServlet/webapprmi");
    if (!cancelledWebforjPoll) {
      browserErrors.add(
          "requestfailed: " + request.method() + " " + request.url() + " (" + failure + ")");
    }
  }
}
