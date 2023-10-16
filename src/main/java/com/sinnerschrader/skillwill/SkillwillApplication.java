package com.sinnerschrader.skillwill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Main Application
 *
 * @author torree
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SkillwillApplication {

  public static void main(String[] args) {
    SpringApplication.run(SkillwillApplication.class, args);
  }

}
