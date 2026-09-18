package com.webforj.tutorial;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class TutorialApp implements AutoCloseable {
  private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(2))
      .build();

  private final Process process;
  private final String baseUrl;
  private final Path logFile;

  private TutorialApp(Process process, String baseUrl, Path logFile) {
    this.process = process;
    this.baseUrl = baseUrl;
    this.logFile = logFile;
  }

  static TutorialApp start() throws Exception {
    requireDocker();

    Path stepPath = Path.of(System.getProperty("e2e.app.dir")).toAbsolutePath().normalize();
    Path jar = Path.of(System.getProperty("e2e.app.jar")).toAbsolutePath().normalize();
    if (!Files.isRegularFile(jar)) {
      throw new IllegalStateException("Application JAR does not exist: " + jar
          + ". Run the complete Maven verify lifecycle to package the application first.");
    }

    int port = findFreePort();
    String baseUrl = "http://127.0.0.1:" + port;
    Path logFile = Path.of(System.getProperty("e2e.output.dir"), "e2e-artifacts", "server.log");
    Files.createDirectories(logFile.getParent());

    String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    ProcessBuilder processBuilder = new ProcessBuilder(
        javaExecutable,
        "-jar",
        jar.toString(),
        "--server.port=" + port,
        "--server.address=127.0.0.1",
        "--spring.jpa.hibernate.ddl-auto=create-drop",
        "--webforj.devtools.browser.open=false",
        "--webforj.devtools.livereload.enabled=false",
        "--webforj.devtools.livereload.static-resources-enabled=false",
        "--webforj.devtools.craftforj.enabled=false",
        "--webforj.debug=false");
    processBuilder.directory(stepPath.toFile());
    processBuilder.redirectErrorStream(true);
    processBuilder.redirectOutput(logFile.toFile());

    Process process = processBuilder.start();
    TutorialApp app = new TutorialApp(process, baseUrl, logFile);
    try {
      app.waitUntilReady();
      return app;
    } catch (Exception exception) {
      app.close();
      throw exception;
    }
  }

  String baseUrl() {
    return baseUrl;
  }

  static void requireDocker() {
    if (!"true".equalsIgnoreCase(System.getenv("E2E_IN_DOCKER"))) {
      throw new IllegalStateException(
          "E2E tests may only run in Docker. Run `mvn verify` from this tutorial step's directory.");
    }
  }

  private static int findFreePort() throws IOException {
    for (int preferredPort : List.of(8080, 8090)) {
      try (ServerSocket ignored = new ServerSocket(preferredPort)) {
        return preferredPort;
      } catch (IOException ignored) {
        // Try the next preferred port.
      }
    }

    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private void waitUntilReady() throws Exception {
    long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/"))
        .timeout(Duration.ofSeconds(3))
        .GET()
        .build();

    while (System.nanoTime() < deadline) {
      if (!process.isAlive()) {
        throw new IllegalStateException(
            "Application exited before becoming ready.\n" + tailLog());
      }

      try {
        HttpResponse<Void> response = HTTP_CLIENT.send(
            request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() < 500) {
          return;
        }
      } catch (IOException ignored) {
        // The server is still starting.
      }

      Thread.sleep(500);
    }

    throw new IllegalStateException(
        "Application did not become ready within " + STARTUP_TIMEOUT + ".\n" + tailLog());
  }

  private String tailLog() {
    try {
      List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
      int start = Math.max(0, lines.size() - 100);
      return "Server log: " + logFile + "\n" + String.join("\n", lines.subList(start, lines.size()));
    } catch (IOException exception) {
      return "Could not read server log " + logFile + ": " + exception.getMessage();
    }
  }

  @Override
  public void close() {
    if (!process.isAlive()) {
      return;
    }

    process.descendants().forEach(ProcessHandle::destroy);
    process.destroy();
    try {
      if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
        process.waitFor(5, TimeUnit.SECONDS);
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
  }
}
