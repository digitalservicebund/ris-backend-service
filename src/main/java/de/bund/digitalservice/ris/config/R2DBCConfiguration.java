package de.bund.digitalservice.ris.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories
@Slf4j
public class R2DBCConfiguration extends AbstractR2dbcConfiguration {

  private Server webServer;

  @Override
  public ConnectionFactory connectionFactory() {
    return ConnectionFactoryBuilder.withUrl(
            "r2dbc:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
        .build();
  }

  @EventListener(ContextRefreshedEvent.class)
  public void start() throws java.sql.SQLException {
    log.info("Starting h2-console at port 8078");
    // Settings to use on localhost:8078 --> Generic H2 (Server), org.h2.Driver, jdbc:h2:mem:testdb
    this.webServer =
        org.h2.tools.Server.createWebServer("-webPort", "8078", "-tcpAllowOthers").start();
  }

  @EventListener(ContextClosedEvent.class)
  public void stop() {
    log.info("Stopping h2-console at port 8078");
    this.webServer.stop();
  }
}
