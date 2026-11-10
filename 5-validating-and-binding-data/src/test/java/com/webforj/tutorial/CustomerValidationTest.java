package com.webforj.tutorial;

import static com.webforj.testbed.assertion.WebforjAssertions.verifyThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.webforj.automation.act.UserInteraction;
import com.webforj.automation.find.ComponentLocator;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextField;
import com.webforj.component.list.ChoiceBox;
import com.webforj.testbed.WebforjTestbed;
import com.webforj.testbed.annotation.WebforjTest;
import com.webforj.tutorial.entity.Customer.Country;
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
class CustomerValidationTest {
  @Autowired
  private CustomerService customers;
  @Autowired
  private CustomerRepository repository;

  @Test
  void validatesInputBeforeSavingTheBoundCustomer(WebforjTestbed context) {
    long before = customers.getTotalCustomersCount();
    context.navigate(FormView.class);
    var firstName = ComponentLocator.of(TextField.class).byLabel("First Name").locate();
    var submit = ComponentLocator.of(Button.class).byText("Submit").locate();

    UserInteraction.type(firstName, "123");
    UserInteraction.type(ComponentLocator.of(TextField.class).byLabel("Last Name").locate(), "Hopper");
    verifyThat(firstName).isInvalid();
    verifyThat(submit).isDisabled();
    assertEquals(before, customers.getTotalCustomersCount());

    UserInteraction.type(firstName, "Grace");
    UserInteraction.type(ComponentLocator.of(TextField.class).byLabel("Company").locate(), "Compiler Systems");
    UserInteraction.selectKey(ComponentLocator.of(ChoiceBox.class).byLabel("Country").locate(), Country.USA);
    verifyThat(firstName).isValid();
    verifyThat(submit).isEnabled();
    UserInteraction.click(submit);

    assertEquals(before + 1, customers.getTotalCustomersCount());
    var saved = repository.findAll().stream()
        .filter(customer -> customer.getFirstName().equals("Grace")).findFirst().orElseThrow();
    assertEquals("Hopper", saved.getLastName());
    assertEquals("Compiler Systems", saved.getCompany());
    assertEquals(Country.USA, saved.getCountry());
  }
}
