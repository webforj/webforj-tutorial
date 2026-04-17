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
  private AppLayout self = getBoundComponent();
  private H1 title = new H1("");
  private ListenerRegistration<NavigateEvent> navigateRegistration;
  private Toolbar toolbar = new Toolbar();
  private AppNav appNav = new AppNav();

  public MainLayout() {
    setHeader();
    setDrawer();
    navigateRegistration = Router.getCurrent().onNavigate(this::onNavigate);
  }

  private void setHeader() {
    self.addToHeader(toolbar);

    toolbar.addToStart(new AppDrawerToggle());
    toolbar.addToTitle(title);
  }

  private void setDrawer() {
    self.setDrawerHeaderVisible(true)
        .addToDrawerTitle(new AppTitle(true));

    appNav.addItem(new AppNavItem("Dashboard", MainView.class,
        TablerIcon.create("archive")));
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
