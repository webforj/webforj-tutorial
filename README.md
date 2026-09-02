
# webforJ Customer Management Tutorial App (Spring Boot)

This project demonstrates how to build a customer management app using the **webforJ framework** with Spring Boot. The app showcases modern web UI development while leveraging Spring’s backend ecosystem, including dependency injection, service layers, and data management.

For a complete step-by-step guide to building this app, see the **[webforJ Tutorial](https://docs.webforj.com/docs/introduction/tutorial/overview)**.

---

## Prerequisites

To run the app, ensure the following tools are installed:

- Java 21 or higher
- BBj 26.02 when running with local BBjServices
- Maven
- Node.js 20 or higher for end-to-end tests
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

The root Playwright suite builds and starts each tutorial step separately, runs it against a fresh in-memory database, and stops it after testing. It uses port 8080 when available, falls back to 8090, and otherwise selects a free local port.

Install the test dependencies and Chromium once:

```sh
npm ci
npm run e2e:install
```

Run all six tutorial steps:

```sh
npm run e2e
```

Run or debug an individual step:

```sh
npm run e2e -- 3
npm run e2e:headed -- 3
```

Update committed screenshot baselines after an intentional visual change:

```sh
npm run e2e:update -- 3
```

Use `--skip-build` while iterating when the packaged application is already current. HTML reports are written under `playwright-report/step-N`, and failure screenshots, videos, traces, and application logs are written under `test-results/step-N`.

## Project Highlights

- **Spring Boot integration:** Autowire Spring beans directly into webforJ views and components.
- **Data binding and validation:** Use standard Java validation annotations and webforJ data binding features.
- **Spring Data support:** Connect UI components to your data layer using Spring repositories.
- **Hot reload:** Automatic browser refresh with Spring DevTools.
- **Familiar development:** Continue using Spring annotations like `@Service`, `@Repository`, and `@Component`.
- **Flexible configuration:** Combine `application.properties` with `webforj.conf` for complete control.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
