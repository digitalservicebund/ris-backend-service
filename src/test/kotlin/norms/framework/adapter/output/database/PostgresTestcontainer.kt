package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import org.testcontainers.containers.PostgreSQLContainer

/**
 * This class implements the Singleton pattern for the postgres database testcontainer to create a single db container
 * which is reused for all tests.
 */
class PostgresTestcontainer private constructor(dockerImageVersion: String) : PostgreSQLContainer<PostgresTestcontainer?>(dockerImageVersion) {
    companion object {
        private var container: PostgresTestcontainer? = null
        val instance: PostgresTestcontainer?
            get() {
                if (container == null) {
                    container = PostgresTestcontainer("postgres:12-alpine")
                }
                return container
            }
    }
}
