package com.webforj.tutorial;

import static com.webforj.testbed.assertion.WebforjAssertions.verifyThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.webforj.automation.act.UserInteraction;
import com.webforj.automation.find.ComponentLocator;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextField;
import com.webforj.component.list.ChoiceBox;
import com.webforj.component.table.Table;
import com.webforj.testbed.WebforjTestbed;
import com.webforj.testbed.annotation.WebforjTest;
import com.webforj.tutorial.entity.Customer.Country;
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
class CustomerNavigationTest {
  @Autowired
  private CustomerService customers;

  @Test
  void createsACustomerAndReturnsToTheTable(WebforjTestbed context) {
    long before = customers.getTotalCustomersCount();
    context.navigate(MainView.class);
    UserInteraction.click(ComponentLocator.of(Button.class).byText("Add Customer").locate());

    UserInteraction.type(ComponentLocator.of(TextField.class).byLabel("First Name").locate(), "Ada");
    UserInteraction.type(ComponentLocator.of(TextField.class).byLabel("Last Name").locate(), "Lovelace");
    UserInteraction.type(ComponentLocator.of(TextField.class).byLabel("Company").locate(), "Analytical Engines");
    UserInteraction.selectKey(ComponentLocator.of(ChoiceBox.class).byLabel("Country").locate(), Country.ENGLAND);
    UserInteraction.click(ComponentLocator.of(Button.class).byText("Submit").locate());

    assertEquals(before + 1, customers.getTotalCustomersCount());
    verifyThat(ComponentLocator.of(Table.class).locate()).hasItemCount((int) before + 1);
    assertEquals("/", context.getRouter().orElseThrow().getHistory()
        .getLocation().orElseThrow().getFullURI());
  }

  @Test
  void cancelsWithoutSaving(WebforjTestbed context) {
    long before = customers.getTotalCustomersCount();
    context.navigate(FormView.class);
    UserInteraction.type(ComponentLocator.of(TextField.class).byLabel("First Name").locate(), "Unsaved");
    UserInteraction.click(ComponentLocator.of(Button.class).byText("Cancel").locate());
    assertEquals(before, customers.getTotalCustomersCount());
    verifyThat(ComponentLocator.of(Table.class).locate()).isAttached();
  }
}
