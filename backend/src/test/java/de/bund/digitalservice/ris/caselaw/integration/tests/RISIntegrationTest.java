package de.bund.digitalservice.ris.caselaw.integration.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.testcontainers.junit.jupiter.Testcontainers;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebFluxTest
@Import({})
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureDataR2dbc
@AutoConfigureWebTestClient(timeout = "PT10S")
public @interface RISIntegrationTest {
  @AliasFor(annotation = Import.class, attribute = "value")
  Class<?>[] imports();

  @AliasFor(annotation = WebFluxTest.class, attribute = "value")
  Class<?>[] controllers() default {};

  @AliasFor(annotation = AutoConfigureWebTestClient.class, attribute = "timeout")
  String timeout() default "PT10S";
}
