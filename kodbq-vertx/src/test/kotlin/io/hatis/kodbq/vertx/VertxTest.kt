package io.hatis.kodbq.vertx

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.vertx.core.Vertx
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlClient
import org.testcontainers.containers.JdbcDatabaseContainer

abstract class VertxTest(
    container: JdbcDatabaseContainer<*>,
    dialect: SqlDialect,
    clientFactory: (vertx: Vertx, container: JdbcDatabaseContainer<*>) -> SqlClient
) : StringSpec({
    container.start()
    val vertx = Vertx.vertx()
    val client = clientFactory(vertx, container)

    val execute: ExecuteAndGetFun = {
        when (this) {
            is SelectBuilder -> {
                val future = execute(client).map { it.map(::rowToMap) }
                future.toCompletionStage().toCompletableFuture().get()
            }
            is InsertBuilder -> {
                val rs = execute(client).toCompletionStage().toCompletableFuture().get()
                listOf(mapOf("affectedRows" to rs.rowCount()))
            }
            is DeleteBuilder, is UpdateBuilder -> {
                val future = execute(client).map { it.rowCount() }
                val affectedRows = future.toCompletionStage().toCompletableFuture().get()
                listOf(mapOf("affectedRows" to affectedRows))
            }
            else -> throw IllegalStateException("Not supported builder")
        }
    }

    beforeTest { kodbqDialect = dialect }

    include(selectsTestFactory(execute))
    include(updateTestFactory(execute))

    afterContainer { container.stop() }
})

private fun rowToMap(row: Row): MutableMap<String, Any> {
    val map = mutableMapOf<String, Any>()
    for (i in 0 until row.size()) {
        row.getValue(i)?.let { map[row.getColumnName(i)] = it }
    }
    return map
}
