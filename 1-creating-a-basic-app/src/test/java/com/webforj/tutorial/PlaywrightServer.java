package com.webforj.tutorial;

import com.microsoft.playwright.impl.driver.jar.DriverJar;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

final class PlaywrightServer extends GenericContainer<PlaywrightServer> {
  private static final int PORT = 3000;

  PlaywrightServer() throws IOException {
    super(DockerImageName.parse("mcr.microsoft.com/playwright/java:v"
        + System.getProperty("playwright.version") + "-noble"));
    withExposedPorts(PORT);
    waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
    withCopyFileToContainer(MountableFile.forHostPath(extractLinuxDriver()), "/opt/playwright");
    withCommand("sh", "-c", "chmod +x /opt/playwright/node && exec "
        + "/opt/playwright/node /opt/playwright/package/cli.js "
        + "run-server --port 3000 --host 0.0.0.0");
  }

  String wsUrl() {
    return "ws://" + getHost() + ":" + getMappedPort(PORT) + "/";
  }

  // The Java dependency bundles the matching Linux server, so the container needs no npm or Maven.
  private static Path extractLinuxDriver() throws IOException {
    String architecture = System.getProperty("os.arch").toLowerCase();
    String platform = architecture.contains("aarch64") || architecture.contains("arm64")
        ? "linux-arm64" : "linux";
    String prefix = "driver/" + platform + "/";
    Path destination = Path.of(System.getProperty("e2e.output.dir"), "playwright-server-driver");
    Path jar;
    try {
      jar = Path.of(DriverJar.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    } catch (Exception e) {
      throw new IOException("Could not locate the Playwright driver bundle", e);
    }

    try (ZipFile bundle = new ZipFile(jar.toFile())) {
      Enumeration<? extends ZipEntry> entries = bundle.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory() || !entry.getName().startsWith(prefix)) {
          continue;
        }
        Path output = destination.resolve(entry.getName().substring(prefix.length())).normalize();
        if (!output.startsWith(destination)) {
          throw new IOException("Invalid Playwright driver entry: " + entry.getName());
        }
        Files.createDirectories(output.getParent());
        try (var input = bundle.getInputStream(entry)) {
          Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
    if (!Files.isRegularFile(destination.resolve("node"))
        || !Files.isRegularFile(destination.resolve("package/cli.js"))) {
      throw new IOException("Playwright Linux server is missing from " + jar);
    }
    return destination;
  }
}
