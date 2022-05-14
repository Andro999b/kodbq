package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.SqlDialect
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.PoolOptions
import org.testcontainers.containers.PostgreSQLContainer

private val container = PostgreSQLContainer("postgres:14.2")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("postgres.sql")

class MutinyVertxTestPostgres: MutinyVertxTest(
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
            PoolOptions().setMaxSize(1)
        )
    }
)