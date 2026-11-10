
# webforJ Customer Management Tutorial App (Spring Boot)

This project demonstrates how to build a customer management app using the **webforJ framework** with Spring Boot. The app showcases modern web UI development while leveraging Spring’s backend ecosystem, including dependency injection, service layers, and data management.

For a complete step-by-step guide to building this app, see the **[webforJ Tutorial](https://docs.webforj.com/docs/introduction/tutorial/overview)**.

---

## Prerequisites

To run the app, ensure the following tools are installed:

- Java 21 or higher
- BBj 26.02 when running with local BBjServices
- Maven
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

## Learning to test with TestBed

The tutorial introduces [TestBed](https://docs.webforj.com/docs/testing/testbed/overview) alongside each feature. The examples run with JUnit and Spring, without a browser, server, Docker, or BBjServices. They require webforJ 26.03 or later and use the BOM-managed `webforj-spring-testbed` dependency.

| Step | New testing concept |
| --- | --- |
| [1](1-creating-a-basic-app/README.md#testing-this-step-with-testbed) | Find components and check a dialog |
| [2](2-working-with-data/README.md#testing-this-step-with-testbed) | Check Spring data in a component |
| [3](3-routing-and-composites/README.md#testing-this-step-with-testbed) | Navigate and submit a form |
| [4](4-observers-and-route-parameters/README.md#testing-this-step-with-testbed) | Exercise route parameters and observers |
| [5](5-validating-and-binding-data/README.md#testing-this-step-with-testbed) | Check validation and data binding |
| [6](6-integrating-an-app-layout/README.md#testing-this-step-with-testbed) | Test the complete app and its layout |

Enter any step directory and run:

```sh
mvn test
```

Each step contains a complete test under `src/test/java/com/webforj/tutorial` and explains its new concepts in its README. Tests find components through visible text or labels and act through `UserInteraction`. Assertions run immediately after the gesture. Each method starts with a fresh TestBed page; tests that change the database roll back their transaction. Spring’s application context remains cached.

TestBed checks application behavior. Use browser tests for appearance, responsive layouts, and client behavior. See [Spring testing](https://docs.webforj.com/docs/integrations/spring/testing) for the integration lifecycle.

## Project Highlights

- **Spring Boot integration:** Autowire Spring beans directly into webforJ views and components.
- **Data binding and validation:** Use standard Java validation annotations and webforJ data binding features.
- **Spring Data support:** Connect UI components to your data layer using Spring repositories.
- **Hot reload:** Automatic browser refresh with Spring DevTools.
- **Familiar development:** Continue using Spring annotations like `@Service`, `@Repository`, and `@Component`.
- **Flexible configuration:** Combine `application.properties` with `webforj.conf` for complete control.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
