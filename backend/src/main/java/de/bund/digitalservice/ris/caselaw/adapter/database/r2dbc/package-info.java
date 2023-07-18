/**
 * This package contains all classes to interact with the database via R2DBC.
 *
 * <p>With the use of Webflux we need a database connection, which can also work in a reactive
 * (non-blocking) way. There a three kind of classes in this package:
 *
 * <ol>
 *   <li>ends with DTO - data transfer object which shows the structure of the database table.
 *   <li>starts with Database and ends with Repository - the interface which use the spring data
 *       repository interface (@see R2dbcRepository) to communicate with the database in a CRUD way.
 *   <li>starts with Postgres and ends with RepositoryImpl - Implementation of the domain repository
 *       interface for handling of domain data.
 * </ol>
 */
package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;
