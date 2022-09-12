package de.bund.digitalservice.ris;

import io.sentry.Sentry;
import java.util.Date;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

  @Generated
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    try {
      throw new Exception("This is a test-exception for Sentry thrown at " + new Date());
    } catch (Exception e) {
      Sentry.captureException(e);
    }
  }
}
