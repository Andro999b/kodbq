package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.jdbcclient.JDBCPool
import io.vertx.mutiny.sqlclient.Row
import io.vertx.sqlclient.PoolOptions
import org.apache.commons.dbcp2.BasicDataSource

abstract class MutinyVertxJDBCTest(url: String, sqlDialect: SqlDialect) : StringSpec({
    val dataSource = BasicDataSource()
    dataSource.url = url
    dataSource.initialSize = 1

    val client = JDBCPool.pool(Vertx.vertx(), dataSource)

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
        kodbqDialect = sqlDialect
    }

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