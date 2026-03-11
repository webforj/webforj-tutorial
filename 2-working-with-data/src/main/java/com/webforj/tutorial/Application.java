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
import com.webforj.tutorial.entity.Customer;
import com.webforj.tutorial.service.CustomerService;
import com.webforj.exceptions.WebforjException;
import com.webforj.component.optiondialog.OptionDialog;
import com.webforj.component.table.Table;

@SpringBootApplication
@StyleSheet("ws://css/card.css")
@AppTheme("system")
@AppProfile(name = "Customer Application", shortName = "CustomerApp")
public class Application extends App {
  private final CustomerService customerService;

  public Application(CustomerService customerService) {
    this.customerService = customerService;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run() throws WebforjException {
    Frame mainFrame = new Frame();
    Paragraph tutorial = new Paragraph("Tutorial App!");
    Button btn = new Button("Info");
    Table<Customer> table = new Table<>();

    mainFrame.setWidth("fit-content");
    mainFrame.addClassName("card");

    table.setSize("1000px", "294px");
    table.setMaxWidth("90vw");
    table.addColumn("firstName", Customer::getFirstName).setLabel("First Name");
    table.addColumn("lastName", Customer::getLastName).setLabel("Last Name");
    table.addColumn("company", Customer::getCompany).setLabel("Company");
    table.addColumn("country", Customer::getCountry).setLabel("Country");
    table.setColumnsToAutoFit();
    table.setColumnsToResizable(false);
    table.getColumns().forEach(column -> column.setSortable(true));
    table.setRepository(customerService.getRepositoryAdapter());

    btn.setTheme(ButtonTheme.PRIMARY)
        .setMaxWidth(200)
        .addClickListener(e -> OptionDialog.showMessageDialog("This is a tutorial!", "Info"));

    mainFrame.add(tutorial, btn, table);
  }

}
