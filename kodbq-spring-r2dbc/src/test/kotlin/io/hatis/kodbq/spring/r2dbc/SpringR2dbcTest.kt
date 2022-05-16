package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.lifecycle.Startable

abstract class SpringR2dbcTest(
    container: Startable,
    dialect: SqlDialect,
    cfoProvider: () -> ConnectionFactoryOptions
): StringSpec({
    container.start()

    val databaseClient = DatabaseClient.builder()
        .connectionFactory(ConnectionFactories.get(cfoProvider()))
        .build()

    val execute: ExecuteAndGetFun = {
        when(this) {
            is SelectBuilder ->
                build(databaseClient)
                    .fetch()
                    .all()
                    .collectList()
                    .block() ?: emptyList()
            is InsertBuilder -> {
                val affectedRows = build(databaseClient)
                    .flatMap { it.fetch().rowsUpdated() }
                    .collectList()
                    .block()
                    ?.sum()
                    ?: 0

                listOf(mapOf("affectedRows" to affectedRows))
            }
            is UpdateBuilder, is DeleteBuilder -> {
                val affectedRows = build(databaseClient)
                    .fetch()
                    .rowsUpdated()
                    .block()!!

                listOf(mapOf("affectedRows" to affectedRows))
            }
            else -> throw IllegalStateException("Not supported builder")
        }
    }

    beforeTest { kodbqDialect = dialect }

    include(selectsTestFactory(execute))
    include(updateTestFactory(execute))

    afterContainer {
        container.stop()
    }
})