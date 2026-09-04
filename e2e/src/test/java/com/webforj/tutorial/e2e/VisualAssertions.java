package com.webforj.tutorial.e2e;

import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;

final class VisualAssertions {
  private static final int DEFAULT_MAX_DIFFERENT_PIXELS = 500;
  private static final int CHANNEL_THRESHOLD = 51;
  private static final int DEFAULT_TIMEOUT_MS = 10_000;
  private static final int POLL_INTERVAL_MS = 250;

  /**
   * Every {@code dwc-icon} resolves its artwork over the network and injects an {@code <svg>} into
   * its own shadow root, so an unresolved icon is invisible in a screenshot while the surrounding
   * DOM already looks complete. Icons live inside nested shadow roots, hence the manual walk.
   */
  private static final String ICONS_RENDERED = """
      () => {
        const icons = [];
        const walk = (root) => {
          for (const element of root.querySelectorAll('*')) {
            if (element.localName === 'dwc-icon') {
              icons.push(element);
            }
            if (element.shadowRoot) {
              walk(element.shadowRoot);
            }
          }
        };
        walk(document);
        return icons.every(icon => icon.shadowRoot && icon.shadowRoot.querySelector('svg'));
      }
      """;

  private VisualAssertions() {
  }

  static void assertScreenshot(Page page, String name) throws IOException {
    requirePngName(name);
    awaitRenderedIcons(page);

    Path baselineDirectory = Path.of(requiredProperty("visual.baseline.dir"));
    Path artifactDirectory = Path.of(requiredProperty("visual.artifact.dir"));
    Path baseline = baselineDirectory.resolve(name);

    if (Boolean.getBoolean("updateScreenshots")) {
      requireDocker();
      Files.createDirectories(baselineDirectory);
      Files.write(baseline, captureStable(page));
      return;
    }

    if (!Files.exists(baseline)) {
      fail("Missing Docker screenshot baseline " + baseline
          + ". Generate it with `mvn verify -DupdateScreenshots=true`.");
    }

    BufferedImage expected = ImageIO.read(baseline.toFile());
    if (expected == null) {
      fail("Could not decode screenshot baseline for " + name);
    }

    // Icons are fetched over the network and components paint asynchronously, so a single capture
    // can catch a half-rendered page. Re-capture until the page matches the baseline or the
    // deadline expires, and report the last mismatch.
    int maximum = Integer.getInteger("visual.maxDiffPixels", DEFAULT_MAX_DIFFERENT_PIXELS);
    long deadline = System.currentTimeMillis() + timeoutMs();
    byte[] screenshot;
    BufferedImage actual;
    int differentPixels;
    BufferedImage diff;

    while (true) {
      screenshot = capture(page);
      actual = ImageIO.read(new ByteArrayInputStream(screenshot));
      if (actual == null) {
        fail("Could not decode captured screenshot for " + name);
      }

      if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
        writeFailureArtifacts(artifactDirectory, name, screenshot, null);
        fail("Screenshot dimensions differ for " + name + ": expected "
            + expected.getWidth() + "x" + expected.getHeight() + ", actual "
            + actual.getWidth() + "x" + actual.getHeight());
      }

      diff = new BufferedImage(actual.getWidth(), actual.getHeight(), BufferedImage.TYPE_INT_ARGB);
      differentPixels = comparePixels(expected, actual, diff);
      if (differentPixels <= maximum) {
        return;
      }
      if (System.currentTimeMillis() >= deadline) {
        break;
      }
      page.waitForTimeout(POLL_INTERVAL_MS);
    }

    writeFailureArtifacts(artifactDirectory, name, screenshot, diff);
    fail("Screenshot mismatch for " + name + ": " + differentPixels
        + " pixels differ (maximum " + maximum + ") after " + timeoutMs()
        + " ms of retries. See " + artifactDirectory);
  }

  private static void awaitRenderedIcons(Page page) {
    try {
      page.waitForFunction(ICONS_RENDERED, null,
          new Page.WaitForFunctionOptions().setTimeout(timeoutMs()));
    } catch (RuntimeException e) {
      fail("Icons did not finish rendering within " + timeoutMs()
          + " ms; screenshots would capture missing icons. Icon artwork is fetched from"
          + " cdn.jsdelivr.net, so check network access from the container.", e);
    }
  }

  private static byte[] capture(Page page) {
    page.evaluate("() => document.fonts.ready");
    return page.screenshot(new Page.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED)
        .setCaret(ScreenshotCaret.HIDE)
        .setScale(ScreenshotScale.CSS));
  }

  /**
   * Captures once the page stops changing, so regenerated baselines never record a transient
   * render. Falls back to the last capture if the page never settles.
   */
  private static byte[] captureStable(Page page) {
    long deadline = System.currentTimeMillis() + timeoutMs();
    byte[] previous = capture(page);
    while (System.currentTimeMillis() < deadline) {
      page.waitForTimeout(POLL_INTERVAL_MS);
      byte[] current = capture(page);
      if (Arrays.equals(previous, current)) {
        return current;
      }
      previous = current;
    }
    return previous;
  }

  private static int comparePixels(BufferedImage expected, BufferedImage actual, BufferedImage diff) {
    int differentPixels = 0;
    for (int y = 0; y < actual.getHeight(); y++) {
      for (int x = 0; x < actual.getWidth(); x++) {
        int expectedRgb = expected.getRGB(x, y);
        int actualRgb = actual.getRGB(x, y);
        if (isDifferent(expectedRgb, actualRgb)) {
          differentPixels++;
          diff.setRGB(x, y, Color.MAGENTA.getRGB());
        } else {
          Color pixel = new Color(actualRgb, true);
          int gray = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3;
          diff.setRGB(x, y, new Color(gray, gray, gray, 110).getRGB());
        }
      }
    }
    return differentPixels;
  }

  private static int timeoutMs() {
    return Integer.getInteger("visual.timeoutMs", DEFAULT_TIMEOUT_MS);
  }

  private static boolean isDifferent(int expectedRgb, int actualRgb) {
    Color expected = new Color(expectedRgb, true);
    Color actual = new Color(actualRgb, true);
    return Math.abs(expected.getRed() - actual.getRed()) > CHANNEL_THRESHOLD
        || Math.abs(expected.getGreen() - actual.getGreen()) > CHANNEL_THRESHOLD
        || Math.abs(expected.getBlue() - actual.getBlue()) > CHANNEL_THRESHOLD
        || Math.abs(expected.getAlpha() - actual.getAlpha()) > CHANNEL_THRESHOLD;
  }

  private static void writeFailureArtifacts(
      Path artifactDirectory, String name, byte[] screenshot, BufferedImage diff) throws IOException {
    Files.createDirectories(artifactDirectory);
    String stem = name.substring(0, name.length() - ".png".length());
    Files.write(artifactDirectory.resolve(stem + "-actual.png"), screenshot);
    if (diff != null) {
      ImageIO.write(diff, "png", artifactDirectory.resolve(stem + "-diff.png").toFile());
    }
  }

  private static String requiredProperty(String name) {
    String value = System.getProperty(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required system property: " + name);
    }
    return value;
  }

  private static void requirePngName(String name) {
    if (!name.matches("[a-z0-9-]+\\.png")) {
      throw new IllegalArgumentException("Screenshot name must be a simple kebab-case PNG filename: " + name);
    }
  }

  private static void requireDocker() {
    if (!"true".equalsIgnoreCase(System.getenv("E2E_IN_DOCKER"))) {
      throw new IllegalStateException("Screenshot baselines may only be generated in Docker.");
    }
  }
}
