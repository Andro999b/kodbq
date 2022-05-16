package io.hatis.kodbq.vertx

import io.hatis.kodbq.SqlDialect
import io.vertx.mssqlclient.MSSQLConnectOptions
import io.vertx.mssqlclient.MSSQLPool
import io.vertx.sqlclient.PoolOptions
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MySQLContainer

private val container = MSSQLServerContainer("mcr.microsoft.com/mssql/server:2017-latest")
    .withInitScript("mssql.sql")

class VertxTestMSSql: VertxTest(
    container,
    SqlDialect.MS_SQL,
    { vertx, container ->
        MSSQLPool.pool(
            vertx,
            MSSQLConnectOptions()
                .setHost(container.host)
                .setPort(container.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT))
                .setUser(container.username)
                .setPassword(container.password),
            PoolOptions().setMaxSize(5)
        )
    }
)