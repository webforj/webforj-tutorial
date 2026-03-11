package com.webforj.tutorial.components;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.H2;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;

public class DrawerHeader extends Composite<FlexLayout> {
  private FlexLayout self = getBoundComponent();
  private H2 title = new H2("Customer Manager");
  private Paragraph subTitle = new Paragraph("A Simple Record System");

  public DrawerHeader() {
    title.setStyle("margin-bottom", "0");
    subTitle.setStyle("color", "#86888f");
    subTitle.setStyle("font-size", ".7em");

    self.setDirection(FlexDirection.COLUMN)
        .setSpacing("0px")
        .add(title, subTitle);
  }
}
