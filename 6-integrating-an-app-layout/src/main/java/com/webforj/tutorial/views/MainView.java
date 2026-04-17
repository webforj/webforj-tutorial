package com.webforj.tutorial.views;

import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.button.ButtonTheme;
import com.webforj.component.layout.flexlayout.FlexAlignment;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.table.Table;
import com.webforj.component.table.event.item.TableItemClickEvent;
import com.webforj.tutorial.entity.Customer;
import com.webforj.tutorial.layouts.MainLayout;
import com.webforj.tutorial.service.CustomerService;
import com.webforj.router.annotation.Route;
import com.webforj.router.history.ParametersBag;
import com.webforj.router.Router;
import com.webforj.router.annotation.FrameTitle;

@Route(value = "/", outlet = MainLayout.class)
@FrameTitle("Customer Table")
public class MainView extends Composite<FlexLayout> {
  private final CustomerService customerService;
  private FlexLayout self = getBoundComponent();
  private Table<Customer> table = new Table<>();
  private Button addCustomer = new Button("Add Customer", ButtonTheme.PRIMARY,
      e -> Router.getCurrent().navigate(FormView.class));

  public MainView(CustomerService customerService) {
    this.customerService = customerService;
    addCustomer.setWidth(200);
    buildTable();
    setFlexLayout();
    self.add(addCustomer, table);
    self.setItemAlignment(FlexAlignment.END, addCustomer);
  }

  private void buildTable() {
    table.setSize("100%", "294px");
    table.addColumn("firstName", Customer::getFirstName).setLabel("First Name");
    table.addColumn("lastName", Customer::getLastName).setLabel("Last Name");
    table.addColumn("company", Customer::getCompany).setLabel("Company");
    table.addColumn("country", Customer::getCountry).setLabel("Country");
    table.setColumnsToAutoFit();
    table.setColumnsToResizable(false);
    table.getColumns().forEach(column -> column.setSortable(true));
    table.setRepository(customerService.getRepositoryAdapter());
    table.setKeyProvider(Customer::getId);
    table.addItemClickListener(this::editCustomer);
  }

  private void setFlexLayout() {
    self.setSize("100%", "100%")
        .setMargin("auto")
        .setMaxWidth(2000)
        .setDirection(FlexDirection.COLUMN)
        .setAlignment(FlexAlignment.CENTER);
  }

  private void editCustomer(TableItemClickEvent<Customer> e) {
    Router.getCurrent().navigate(FormView.class,
        ParametersBag.of("id=" + e.getItemKey()));
  }
}
