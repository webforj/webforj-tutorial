package com.webforj.tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.webforj.App;
import com.webforj.annotation.AppProfile;
import com.webforj.annotation.AppTheme;
import com.webforj.annotation.StyleSheet;
import com.webforj.annotation.Routify;

@SpringBootApplication
@StyleSheet("ws://css/card.css")
@AppTheme("system")
@Routify(packages = "com.webforj.tutorial.views")
@AppProfile(name = "Customer Application", shortName = "CustomerApp")
public class Application extends App {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
