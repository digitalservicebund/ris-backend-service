package de.bund.digitalservice.ris.caselaw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("NeuRIS Service API")
                .version("0.0.1")
                .description("Interface for the NeuRIS service (legal information system)")
                .contact(
                    new Contact()
                        .name("DigitalService GmbH des Bundes")
                        .url("https://digitalservice.bund.de")
                        .email("hallo@digitalservice.bund.de")))
        .addServersItem(
            new Server().description("Local development server").url("http://127.0.0.1"));
  }
}
