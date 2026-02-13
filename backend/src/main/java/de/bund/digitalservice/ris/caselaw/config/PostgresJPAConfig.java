package de.bund.digitalservice.ris.caselaw.config;

import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.TransactionManager;

@Configuration
@EnableJpaRepositories(
    basePackages = "de.bund.digitalservice.ris.caselaw.adapter.database.jpa",
    transactionManagerRef = "jpaTransactionManager")
public class PostgresJPAConfig {
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

  @Value("${database.schema:caselaw}")
  private String schema;

  @Bean
  public DataSource dataSource() {
    return DataSourceBuilder.create()
        .driverClassName("org.postgresql.Driver")
        .url("jdbc:postgresql://" + host + ":" + port + "/" + database + "?currentSchema=" + schema)
        .username(user)
        .password(password)
        .build();
  }

  @Bean
  public TransactionManager jpaTransactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
    return transactionManager;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
        new LocalContainerEntityManagerFactoryBean();
    entityManagerFactoryBean.setDataSource(dataSource());
    entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    entityManagerFactoryBean.setPackagesToScan(
        "de.bund.digitalservice.ris.caselaw.adapter.database.jpa");

    return entityManagerFactoryBean;
  }
}
