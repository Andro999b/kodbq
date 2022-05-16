package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.SqlDialect
import org.testcontainers.containers.MSSQLR2DBCDatabaseContainer
import org.testcontainers.containers.MSSQLR2DBCDatabaseContainerProvider
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer

private val container = MSSQLServerContainer("mcr.microsoft.com/mssql/server:2017-latest")
    .withInitScript("mssql.sql")

class SpringR2dbcTestMSSql: SpringR2dbcTest(
    container,
    SqlDialect.MS_SQL,
    { MSSQLR2DBCDatabaseContainer.getOptions(container) }
)