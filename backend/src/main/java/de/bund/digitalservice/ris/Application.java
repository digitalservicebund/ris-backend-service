package de.bund.digitalservice.ris;

import io.sentry.Sentry;
import java.util.Date;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    try {
      throw new Exception("This is a test-exception for Sentry thrown at " + new Date());
    } catch (Exception e) {
      Sentry.captureException(e);
    }
  }
}
