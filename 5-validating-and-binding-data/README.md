
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

## Testing this step with TestBed

### Check validation and data binding

Use the same gestures to exercise BindingContext and Jakarta validation. Invalid characters mark First Name as invalid and disable Submit. Correct the value, choose a country, and submit; the test checks every bound value on the saved customer.

The runnable example combines Spring and TestBed:

```java
@SpringBootTest
@ActiveProfiles("test")
@WebforjTest(routeClasses = {MainView.class, FormView.class})
@Transactional
class CustomerValidationTest {
  // See the complete example linked below.
}
```

The following excerpt comes after the test locates both fields and enters the required last name. Each interaction completes synchronously, so assertions need no sleeps:

```java
UserInteraction.type(firstName, "123");
verifyThat(firstName).isInvalid();
verifyThat(submit).isDisabled();

UserInteraction.type(firstName, "Grace");
verifyThat(firstName).isValid();
verifyThat(submit).isEnabled();
```

Read the complete [CustomerValidationTest.java](src/test/java/com/webforj/tutorial/CustomerValidationTest.java) example, including its imports and assertions. Run it from this step directory:

```sh
mvn test
```

Each method gets a fresh TestBed page. The test profile disables development tools. Spring keeps its context and service beans across methods; `@Transactional` rolls back each test’s database changes, keeping the seed data available for the next method. Browser tests are still needed to check CSS, responsive layout, and browser behavior.

For the underlying APIs, see [TestBed](https://docs.webforj.com/docs/testing/testbed/overview) and [Spring testing](https://docs.webforj.com/docs/integrations/spring/testing).

## Project Highlights

- **Spring Boot integration:** Autowire Spring beans directly into webforJ views and components.
- **Data binding and validation:** Use standard Java validation annotations and webforJ data binding features.
- **Spring Data support:** Connect UI components to your data layer using Spring repositories.
- **Hot reload:** Automatic browser refresh with Spring DevTools.
- **Familiar development:** Continue using Spring annotations like `@Service`, `@Repository`, and `@Component`.
- **Flexible configuration:** Combine `application.properties` with `webforj.conf` for complete control.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
