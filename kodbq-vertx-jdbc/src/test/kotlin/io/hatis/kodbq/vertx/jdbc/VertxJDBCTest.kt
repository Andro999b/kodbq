package io.hatis.kodbq.vertx.jdbc

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import org.apache.commons.dbcp2.BasicDataSource

abstract class VertxJDBCTest(
    url: String,
    dialect: SqlDialect
) : StringSpec({
    val dataSource = BasicDataSource()
    dataSource.url = url
    dataSource.initialSize = 1

    val client = JDBCPool.pool(Vertx.vertx(), dataSource)

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
})

private fun rowToMap(row: Row): MutableMap<String, Any> {
    val map = mutableMapOf<String, Any>()
    for (i in 0 until row.size()) {
        row.getValue(i)?.let { map[row.getColumnName(i)] = it }
    }
    return map
}
