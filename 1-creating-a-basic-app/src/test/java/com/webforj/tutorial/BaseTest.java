package com.webforj.tutorial;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class BaseTest {
  private static PlaywrightServer server;
  private static Playwright playwright;
  private static Browser browser;

  private BrowserContext context;
  private final List<String> browserErrors = new ArrayList<>();

  @LocalServerPort
  private int port;

  protected Page page;

  @BeforeAll
  static void startBrowser() throws IOException {
    server = new PlaywrightServer();
    server.start();
    try {
      playwright = Playwright.create();
      browser = playwright.chromium().connect(server.wsUrl(),
          new BrowserType.ConnectOptions().setExposeNetwork("<loopback>"));
    } catch (RuntimeException exception) {
      server.stop();
      throw exception;
    }
    PlaywrightAssertions.setDefaultAssertionTimeout(10_000);
  }

  @BeforeEach
  void createBrowserContext() {
    browserErrors.clear();
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
    if (context != null) {
      Path traceDirectory = Path.of(System.getProperty("e2e.output.dir"), "playwright-traces");
      Files.createDirectories(traceDirectory);
      String testName = (getClass().getSimpleName() + "-" + testInfo.getDisplayName())
          .replaceAll("[^a-zA-Z0-9.-]", "-");
      context.tracing().stop(new Tracing.StopOptions()
          .setPath(traceDirectory.resolve(testName + ".zip")));
      context.close();
      context = null;
    }
    assertTrue(errorsBeforeClose.isEmpty(),
        () -> "Browser runtime/network errors:\n" + String.join("\n", errorsBeforeClose));
  }

  @AfterAll
  static void stopBrowser() {
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
    if (server != null) {
      server.stop();
    }
  }

  protected void openApplication() {
    openApplication("/");
  }

  protected void openApplication(String path) {
    String normalizedPath = path.startsWith("/") ? path : "/" + path;
    page.navigate("http://127.0.0.1:" + port + normalizedPath,
        new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
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
