package io.hatis.kodbq.vertx.jdbc

import io.hatis.kodbq.SqlDialect
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import org.testcontainers.containers.PostgreSQLContainer


private val container = PostgreSQLContainer("postgres:14.2")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("postgres.sql")

class VertxTestPostgres: VertxTest(
    container,
    SqlDialect.PG,
    { vertx, container ->
        PgPool.pool(
            vertx,
            PgConnectOptions()
                .setHost(container.host)
                .setPort(container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
                .setDatabase(container.databaseName)
                .setUser(container.username)
                .setPassword(container.password)
            ,
            PoolOptions().setMaxSize(5)
        )
    }
)