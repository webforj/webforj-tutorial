package com.webforj.tutorial;

import static com.webforj.testbed.assertion.WebforjAssertions.verifyThat;

import com.webforj.automation.act.UserInteraction;
import com.webforj.automation.find.ComponentLocator;
import com.webforj.component.button.Button;
import com.webforj.component.html.elements.H1;
import com.webforj.component.layout.appnav.AppNavItem;
import com.webforj.component.table.Table;
import com.webforj.testbed.WebforjTestbed;
import com.webforj.testbed.annotation.WebforjTest;
import com.webforj.tutorial.layouts.MainLayout;
import com.webforj.tutorial.views.MainView;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@WebforjTest(app = Application.class)
@Transactional
class ApplicationLayoutTest {
  @Test
  void keepsTheLayoutWhileNavigatingBetweenViews(WebforjTestbed context) {
    context.navigate(MainView.class);
    var layout = ComponentLocator.of(MainLayout.class).locate();
    verifyThat(ComponentLocator.of(H1.class).within(layout).locate()).hasText("Customer Table");

    UserInteraction.click(ComponentLocator.of(AppNavItem.class).byText("About").locate());
    verifyThat(ComponentLocator.of(H1.class).within(layout).locate()).hasText("About");
    verifyThat(layout).isAttached();

    UserInteraction.click(ComponentLocator.of(AppNavItem.class).byText("Dashboard").locate());
    verifyThat(ComponentLocator.of(H1.class).within(layout).locate()).hasText("Customer Table");
    verifyThat(ComponentLocator.of(Table.class).locate()).isAttached();
  }
}
