package de.bund.digitalservice.ris.config;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.PROTOCOL;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories
public class PostgresConfig extends AbstractR2dbcConfiguration {
  @Value("${database.user:test}")
  private String user;

  @Value("${database.password:test}")
  private String password;

  @Value("${database.host:localhost}")
  private String host;

  @Value("${database.port:5432}")
  private Integer port;

  @Value("${database.database:neuris}")
  private String database;

  @Override
  @NotNull
  @Bean
  public ConnectionFactory connectionFactory() {
    return ConnectionFactoryBuilder.withOptions(
            ConnectionFactoryOptions.builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "postgresql")
                .option(HOST, host)
                .option(PORT, port)
                .option(USER, user)
                .option(PASSWORD, password)
                .option(DATABASE, database))
        .build();
  }
}
