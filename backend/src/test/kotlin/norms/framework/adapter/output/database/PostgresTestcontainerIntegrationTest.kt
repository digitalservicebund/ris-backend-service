package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import org.junit.jupiter.api.Tag
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

/**
 * This class is a base class for integration tests which require a PostgreSQL database testcontainer.
 * All test classes inheriting from this class will automatically use the same database container instance.
 *
 * Instead of letting testcontainers start the database container automatically just by configuration, it is created
 * manually here due to the following reasons:
 * - All integration tests shall reuse the same database instance to speed up the tests
 * - Flyway does not support R2DBC and therefore uses JDBC to access the database which results in the Flyway migrations
 * being executed on a different instance than the tests when using automatic start of containers.
 * @see <a href="https://github.com/testcontainers/testcontainers-java/issues/4473">Testcontainers GitHub Issue</a>
 */
@Tag("integration")
open class PostgresTestcontainerIntegrationTest {
    companion object {
        @Container
        val postgresql = PostgresTestcontainer.instance!!.apply {
            withDatabaseName("testdb")
            withUsername("user")
            withPassword("password")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun postgresqlProperties(registry: DynamicPropertyRegistry) {
            val r2dbcUrl = String.format(
                "r2dbc:postgresql://%s:%s/%s",
                postgresql.host,
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.databaseName
            )

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgresql.username }
            registry.add("spring.r2dbc.password") { postgresql.password }

            registry.add("database.host") { postgresql.host }
            registry.add("database.port") { postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) }
            registry.add("database.user") { postgresql.username }
            registry.add("database.password") { postgresql.password }
            registry.add("database.database") { postgresql.databaseName }
        }
    }
}
