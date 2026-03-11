package com.webforj.tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.webforj.App;
import com.webforj.annotation.AppProfile;
import com.webforj.annotation.AppTheme;
import com.webforj.annotation.StyleSheet;
import com.webforj.component.button.Button;
import com.webforj.component.button.ButtonTheme;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.component.window.Frame;
import com.webforj.exceptions.WebforjException;
import com.webforj.component.optiondialog.OptionDialog;

@SpringBootApplication
@StyleSheet("ws://css/card.css")
@AppTheme("system")
@AppProfile(name = "Customer Application", shortName = "CustomerApp")
public class Application extends App {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run() throws WebforjException {
    Frame mainFrame = new Frame();
    Paragraph tutorial = new Paragraph("Tutorial App!");
    Button btn = new Button("Info");

    btn.setTheme(ButtonTheme.PRIMARY)
        .setMaxWidth(200)
        .addClickListener(e -> OptionDialog.showMessageDialog("This is a tutorial!", "Info"));

    mainFrame.setWidth("fit-content")
        .addClassName("card")
        .add(tutorial, btn);
  }
}
