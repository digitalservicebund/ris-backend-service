package de.bund.digitalservice.ris.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@TestConfiguration
@EnableR2dbcRepositories
public class R2DBCTestConfig extends AbstractR2dbcConfiguration {

  @Override
  public ConnectionFactory connectionFactory() {
    return ConnectionFactoryBuilder.withUrl(
            "r2dbc:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
        .build();
  }
}
