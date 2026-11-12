package com.webforj.tutorial.layouts;

import com.webforj.router.Router;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;
import com.webforj.router.event.NavigateEvent;

import com.webforj.component.Component;
import com.webforj.component.Composite;
import com.webforj.component.html.elements.H1;
import com.webforj.component.icons.TablerIcon;
import com.webforj.component.layout.applayout.AppDrawerToggle;
import com.webforj.component.layout.applayout.AppLayout;
import com.webforj.component.layout.appnav.AppNav;
import com.webforj.component.layout.appnav.AppNavItem;
import com.webforj.component.layout.toolbar.Toolbar;
import com.webforj.tutorial.components.AppTitle;
import com.webforj.tutorial.views.AboutView;
import com.webforj.tutorial.views.MainView;
import com.webforj.dispatcher.ListenerRegistration;

@Route
public class MainLayout extends Composite<AppLayout> {
  private final AppLayout self = getBoundComponent();
  private final H1 title = new H1("");
  private final ListenerRegistration<NavigateEvent> navigateRegistration;

  public MainLayout() {
    setHeader();
    setDrawer();
    navigateRegistration = Router.getCurrent().onNavigate(this::onNavigate);
  }

  private void setHeader() {
    Toolbar toolbar = new Toolbar();
    self.addToHeader(toolbar);

    toolbar.addToStart(new AppDrawerToggle(TablerIcon.create("layout-sidebar")));
    toolbar.addToTitle(title);
  }

  private void setDrawer() {
    self.setDrawerHeaderVisible(true)
        .addToDrawerTitle(new AppTitle(true));

    AppNav appNav = new AppNav();
    appNav.addItem(new AppNavItem("Dashboard", MainView.class,
        TablerIcon.create("layout-dashboard")));
    appNav.addItem(new AppNavItem("About", AboutView.class,
        TablerIcon.create("info-circle")));
    self.addToDrawer(appNav);
  }

  @Override
  protected void onDidDestroy() {
    if (navigateRegistration != null) {
      navigateRegistration.remove();
    }
  }

  private void onNavigate(NavigateEvent ev) {
    Component component = ev.getContext().getComponent();
    if (component != null) {
      FrameTitle frameTitle = component.getClass().getAnnotation(FrameTitle.class);
      title.setText(frameTitle != null ? frameTitle.value() : "");
    }
  }

}
