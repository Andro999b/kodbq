package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.SqlDialect
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer

private val container = PostgreSQLContainer("postgres:14.2")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("postgres.sql")

class SpringR2dbcTestPostgres: SpringR2dbcTest(
    container,
    SqlDialect.PG,
    { PostgreSQLR2DBCDatabaseContainer.getOptions(container) }
)