package io.hatis.kodbq.vertx.jdbc

import io.hatis.kodbq.SqlDialect
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.mysqlclient.impl.MySQLPoolOptions
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer

private val container = MySQLContainer("mysql:8.0")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("mysql.sql")

class VertxTestMysql: VertxTest(
    container,
    SqlDialect.MY_SQL,
    { vertx, container ->
        MySQLPool.pool(
            vertx,
            MySQLConnectOptions()
                .setHost(container.host)
                .setPort(container.getMappedPort(MySQLContainer.MYSQL_PORT))
                .setDatabase(container.databaseName)
                .setUser(container.username)
                .setPassword(container.password),
            PoolOptions().setMaxSize(5)
        )
    }
)