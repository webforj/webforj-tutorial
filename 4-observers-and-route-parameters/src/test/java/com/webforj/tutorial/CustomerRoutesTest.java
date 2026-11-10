package com.webforj.tutorial;

import static com.webforj.testbed.assertion.WebforjAssertions.verifyThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.webforj.automation.act.UserInteraction;
import com.webforj.automation.find.ComponentLocator;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextField;
import com.webforj.router.history.ParametersBag;
import com.webforj.testbed.WebforjTestbed;
import com.webforj.testbed.annotation.WebforjTest;
import com.webforj.tutorial.repository.CustomerRepository;
import com.webforj.tutorial.service.CustomerService;
import com.webforj.tutorial.views.FormView;
import com.webforj.tutorial.views.MainView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@WebforjTest(routeClasses = {MainView.class, FormView.class})
@Transactional
class CustomerRoutesTest {
  @Autowired
  private CustomerRepository repository;
  @Autowired
  private CustomerService customers;

  @Test
  void loadsAndUpdatesTheCustomerNamedByTheRoute(WebforjTestbed context) {
    var customer = repository.findAll().getFirst();
    context.navigate(FormView.class, ParametersBag.of("id=" + customer.getId()));

    verifyThat(ComponentLocator.of(TextField.class).byLabel("First Name").locate())
        .hasValue(customer.getFirstName());
    var company = ComponentLocator.of(TextField.class).byLabel("Company").locate();
    verifyThat(company).hasValue(customer.getCompany());
    UserInteraction.type(company, "Updated Company");
    UserInteraction.click(ComponentLocator.of(Button.class).byText("Submit").locate());

    assertEquals("Updated Company", customers.getCustomerByKey(customer.getId()).getCompany());
  }

  @Test
  void redirectsAnUnknownCustomerToTheTable(WebforjTestbed context) {
    assertInstanceOf(MainView.class, context.navigate("customer/999999"));
    assertEquals("/", context.getRouter().orElseThrow().getHistory()
        .getLocation().orElseThrow().getFullURI());
  }
}
