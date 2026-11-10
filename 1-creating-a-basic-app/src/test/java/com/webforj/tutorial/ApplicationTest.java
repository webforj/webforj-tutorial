package com.webforj.tutorial;

import static com.webforj.testbed.assertion.WebforjAssertions.verifyThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.webforj.automation.act.UserInteraction;
import com.webforj.automation.find.ComponentLocator;
import com.webforj.component.button.Button;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.testbed.WebforjTestbed;
import com.webforj.testbed.annotation.WebforjTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WebforjTest(app = Application.class)
class ApplicationTest {
  @Test
  void opensTheInformationDialog(WebforjTestbed context) {
    var greeting = ComponentLocator.of(Paragraph.class).byText("Tutorial App!").locate();
    verifyThat(greeting).isAttached();

    var info = ComponentLocator.of(Button.class).byText("Info").locate();
    UserInteraction.click(info);

    // Message dialogs are dismissed automatically; their contents remain available to the test.
    assertEquals(1, context.getShownOptionDialogs().size());
    var dialog = context.getShownOptionDialogs().getFirst();
    assertEquals("Info", dialog.getTitle());
    assertEquals("This is a tutorial!", dialog.getMessage());
  }
}
