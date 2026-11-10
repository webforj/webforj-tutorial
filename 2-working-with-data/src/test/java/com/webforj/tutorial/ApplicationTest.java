package com.webforj.tutorial;

import static com.webforj.testbed.assertion.WebforjAssertions.verifyThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.webforj.automation.act.UserInteraction;
import com.webforj.automation.find.ComponentLocator;
import com.webforj.component.button.Button;
import com.webforj.component.table.Table;
import com.webforj.testbed.WebforjTestbed;
import com.webforj.testbed.annotation.WebforjTest;
import com.webforj.tutorial.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@WebforjTest(app = Application.class)
@Transactional
class ApplicationTest {
  @Autowired
  private CustomerService customers;

  @Test
  void showsTheCustomersLoadedBySpring(WebforjTestbed context) {
    var table = ComponentLocator.of(Table.class).locate();
    assertTrue(customers.getTotalCustomersCount() > 0);
    verifyThat(table).isAttached().hasItemCount((int) customers.getTotalCustomersCount());

    UserInteraction.click(ComponentLocator.of(Button.class).byText("Info").locate());
    assertEquals("This is a tutorial!", context.getShownOptionDialogs().getFirst().getMessage());
  }
}
