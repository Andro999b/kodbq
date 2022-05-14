package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.SqlClient
import org.testcontainers.containers.JdbcDatabaseContainer

abstract class MutinyVertxTest(
    container: JdbcDatabaseContainer<*>,
    dialect: SqlDialect,
    clientFactory: (vertx: Vertx, container: JdbcDatabaseContainer<*>) -> SqlClient
) : StringSpec({
    container.start()

    val client = clientFactory(Vertx.vertx(), container)

    val execute: ExecuteAndGetFun = {
        when (this) {
            is SelectBuilder -> execute(client).await().indefinitely().map(::rowToMap)
            is InsertBuilder -> {
                val affectedRows = execute(client).await().indefinitely().rowCount()
                listOf(mapOf("affectedRows" to affectedRows))
            }
            is DeleteBuilder -> {
                val affectedRows = execute(client).await().indefinitely().rowCount()
                listOf(mapOf("affectedRows" to affectedRows))
            }
            is UpdateBuilder -> {
                val affectedRows = execute(client).await().indefinitely().rowCount()
                listOf(mapOf("affectedRows" to affectedRows))
            }
            else -> throw IllegalStateException("Not supported builder")
        }
    }

    beforeTest {
        kodbqDialect = dialect
    }

    include(selectsTestFactory(execute))
    include(updateTestFactory(execute))

    afterContainer {
        container.stop()
    }
})

private fun rowToMap(row: Row): MutableMap<String, Any> {
    val map = mutableMapOf<String, Any>()
    for (i in 0 until row.size()) {
        row.getValue(i)?.let { map[row.getColumnName(i)] = it }
    }
    return map
}