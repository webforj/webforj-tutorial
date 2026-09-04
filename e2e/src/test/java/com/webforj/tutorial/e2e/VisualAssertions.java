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
    byte[] screenshot = captureStable(page, name, artifactDirectory);

    if (Boolean.getBoolean("updateScreenshots")) {
      requireDocker();
      Files.createDirectories(baselineDirectory);
      Files.write(baseline, screenshot);
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

    BufferedImage actual = ImageIO.read(new ByteArrayInputStream(screenshot));
    if (actual == null) {
      fail("Could not decode captured screenshot for " + name);
    }

    if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
      writeFailureArtifacts(artifactDirectory, name, screenshot, null);
      fail("Screenshot dimensions differ for " + name + ": expected "
          + expected.getWidth() + "x" + expected.getHeight() + ", actual "
          + actual.getWidth() + "x" + actual.getHeight());
    }

    BufferedImage diff = new BufferedImage(
        actual.getWidth(), actual.getHeight(), BufferedImage.TYPE_INT_ARGB);
    int differentPixels = comparePixels(expected, actual, diff);
    int maximum = Integer.getInteger("visual.maxDiffPixels", DEFAULT_MAX_DIFFERENT_PIXELS);
    if (differentPixels > maximum) {
      writeFailureArtifacts(artifactDirectory, name, screenshot, diff);
      fail("Screenshot mismatch for " + name + ": " + differentPixels
          + " pixels differ (maximum " + maximum + "). See " + artifactDirectory);
    }
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

  private static byte[] captureStable(
      Page page, String name, Path artifactDirectory) throws IOException {
    long deadline = System.nanoTime() + timeoutMs() * 1_000_000L;
    byte[] previous = capture(page);
    while (System.nanoTime() < deadline) {
      page.waitForTimeout(POLL_INTERVAL_MS);
      byte[] current = capture(page);
      if (Arrays.equals(previous, current)) {
        return current;
      }
      previous = current;
    }

    writeFailureArtifacts(artifactDirectory, name, previous, null);
    return fail("Page did not produce two consecutive identical screenshots for " + name
        + " within " + timeoutMs() + " ms. Refusing to compare or update an unstable image. See "
        + artifactDirectory);
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
