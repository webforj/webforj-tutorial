
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

The root Maven project runs the Java Playwright suite exclusively in Docker. The container packages and starts each tutorial step separately, uses Chromium from the pinned official Playwright Java image, and stops the application after that step's tests. Applications prefer port 8080, fall back to 8090, and otherwise select a free container port.

Run all six tutorial steps, including screenshot comparisons:

```sh
mvn verify
```

Run an individual step in Docker:

```sh
mvn verify -Dit.test=Step3IT
```

After an intentional visual change, regenerate every committed baseline in the same Docker environment:

```sh
mvn verify -DupdateScreenshots=true
```

You can also use the documentation-style explicit Docker goals. Build the image once, then run or update tests with the existing image:

```sh
mvn exec:exec@docker-e2e-build
mvn exec:exec@docker-e2e
mvn exec:exec@docker-e2e-single -Dtest=Step3IT
mvn exec:exec@docker-e2e-update
```

Screenshot baselines are stored in `e2e/src/test/resources/screenshots`. The comparator allows up to 500 anti-aliased pixels to differ in each 1440×900 capture; failure images are written to `target/visual-diffs`, Playwright traces to `target/playwright-traces`, and application logs to `target/e2e-artifacts`. The inner test profile also rejects execution unless `E2E_IN_DOCKER=true`, preventing accidental host-generated baselines.

## Project Highlights

- **Spring Boot integration:** Autowire Spring beans directly into webforJ views and components.
- **Data binding and validation:** Use standard Java validation annotations and webforJ data binding features.
- **Spring Data support:** Connect UI components to your data layer using Spring repositories.
- **Hot reload:** Automatic browser refresh with Spring DevTools.
- **Familiar development:** Continue using Spring annotations like `@Service`, `@Repository`, and `@Component`.
- **Flexible configuration:** Combine `application.properties` with `webforj.conf` for complete control.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
