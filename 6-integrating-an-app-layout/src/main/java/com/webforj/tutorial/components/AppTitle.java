package com.webforj.tutorial.components;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.H1;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;

public class AppTitle extends Composite<FlexLayout> {

  public AppTitle(boolean showSubTitle) {
    FlexLayout self = getBoundComponent();
    H1 title = new H1("Customer Manager");
    Paragraph subTitle = new Paragraph("A Simple Record System");
    title.setStyle("margin", "0");
    subTitle.setStyle("color", "var(--dwc-color-gray-text-light)");
    subTitle.setStyle("font-size", "var(--dwc-font-size-xs)");
    subTitle.setStyle("margin", "0");

    self.setDirection(FlexDirection.COLUMN)
        .setSpacing("0")
        .add(title);

    if (showSubTitle) {
      self.add(subTitle);
    }
  }
}
