package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.SqlDialect
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer

private val container = MySQLContainer("mysql:8.0")
    .withCommand("--default-authentication-plugin=mysql_native_password")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("mysql.sql")


class SpringR2dbcTestMySql: SpringR2dbcTest(
    container,
    SqlDialect.MY_SQL,
    { MySQLR2DBCDatabaseContainer.getOptions(container) }
)