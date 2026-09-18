
# webforJ Customer Management Tutorial App (Spring Boot)

This project demonstrates how to build a customer management app using the **webforJ framework** with Spring Boot. The app showcases modern web UI development while leveraging Spring’s backend ecosystem, including dependency injection, service layers, and data management.

For a complete step-by-step guide to building this app, see the **[webforJ Tutorial](https://docs.webforj.com/docs/introduction/tutorial/overview)**.

---

## Prerequisites

To run the app, ensure the following tools are installed:

- Java 21 or higher
- BBj 26.02 when running with local BBjServices
- Maven
- Docker Desktop (or another Docker engine) for end-to-end tests
- A Java IDE (e.g., IntelliJ IDEA, Eclipse, VSCode)
- Web browser
- Git (recommended)

---

## Project Structure

```bash
webforj-tutorial
│   .gitignore
│   LICENSE
│   README.md
│   tree.txt
│
├───1-creating-a-basic-app
├───2-working-with-data
├───3-routing-and-composites
├───4-observers-and-route-parameters
├───5-validating-and-binding-data  
└───6-integrating-an-app-layout 
```

## Running the App (Spring Boot)

1. Navigate to the desired step directory (e.g., `1-creating-a-basic-app`):
	```sh
	cd 1-creating-a-basic-app
	```
2. Start the Spring Boot application:
	```sh
	mvn
	```
3. Open your browser and go to [http://localhost:8080](http://localhost:8080).

## End-to-End and Screenshot Tests

Each tutorial step owns its Java Playwright tests in `src/test/java/com/webforj/tutorial`, mirroring the application's Java package, and its screenshot baselines in `src/test/resources/screenshots`. Test helpers are included in each step so the step remains self-contained, with no dependency on a sibling project.

With Docker running, enter the step you want to test and run the normal Maven verification lifecycle:

```sh
cd 3-routing-and-composites
mvn verify
```

This builds and tests only that step. Maven launches the pinned official Playwright Java Docker image with the step directory mounted at `/app`. Inside the container, Maven performs a clean build and runs the Java Playwright tests and screenshot comparisons with Failsafe. No root POM or custom Docker image is needed.

To select a test class or method within the current step:

```sh
mvn verify "-Dit.test=Step3IT#createsCustomerAndReturnsToTable"
```

After an intentional visual change, regenerate this step's baselines in the same Docker environment:

```sh
mvn verify -DupdateScreenshots=true
```

GitHub Actions runs `scripts/run-e2e.sh`, which enters each step and invokes `mvn -B -ntp verify`. It continues after a failed step and exits unsuccessfully if any step failed. You can run the same script from the repository root using Bash (Git Bash on Windows):

```sh
bash scripts/run-e2e.sh
```

The workflow's optional `step` input selects one step; scheduled runs test all steps.

In each step, test reports are written to `target/failsafe-reports`, failure images to `target/visual-diffs`, Playwright traces to `target/playwright-traces`, and application logs to `target/e2e-artifacts`. The screenshot comparator allows up to 500 pixels beyond its per-channel tolerance to differ.

Each test starts a fresh application and browser context and stops them afterward. Applications prefer port 8080, fall back to 8090, and otherwise select a free container port. `mvn test` runs unit tests, while `mvn verify` also runs the E2E tests in Docker. Standard `-DskipITs`, `-DskipTests`, and `-Dmaven.test.skip=true` flags skip the Docker run when requested.

The `container-e2e` profile binds Failsafe only for the inner Docker run, and the test setup rejects execution unless `E2E_IN_DOCKER=true` before launching Playwright. Baseline generation uses this same guard. Each step's `playwright.version` selects both the Java dependency and the Docker image tag.

## Project Highlights

- **Spring Boot integration:** Autowire Spring beans directly into webforJ views and components.
- **Data binding and validation:** Use standard Java validation annotations and webforJ data binding features.
- **Spring Data support:** Connect UI components to your data layer using Spring repositories.
- **Hot reload:** Automatic browser refresh with Spring DevTools.
- **Familiar development:** Continue using Spring annotations like `@Service`, `@Repository`, and `@Component`.
- **Flexible configuration:** Combine `application.properties` with `webforj.conf` for complete control.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
