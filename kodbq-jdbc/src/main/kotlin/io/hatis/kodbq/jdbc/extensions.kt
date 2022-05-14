package io.hatis.kodbq.jdbc

import io.hatis.kodbq.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

fun <T> execute(connection: Connection, sqlAndParams: Pair<String, List<Any?>>, mapper: (rs: ResultSet) -> T): T {
    val (sql, params) = sqlAndParams
    return connection.prepareStatement(sql).use { statement ->
        params.forEachIndexed { i, v -> statement.setObject(i + 1, v) }
        mapper(statement.executeQuery())
    }
}

fun executeUpdate(connection: Connection, sqlAndParams: Pair<String, List<Any?>>): Int {
    val (sql, params) = sqlAndParams
    return connection.prepareStatement(sql).use { statement ->
        params.forEachIndexed { i, v -> statement.setObject(i + 1, v) }
        statement.executeUpdate()
    }
}


fun executeBatch(
    connection: Connection,
    sqlAndParams: Pair<String, List<List<Any?>>>,
    generatedKeys: Set<String>
): InsertResult {
    val (sql, params) = sqlAndParams

    var generatedKeysResult: List<Map<String, Any>> = emptyList()
    var affectedRows = 0

    val prepareStatement = if (generatedKeys.isNotEmpty())
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
    else
        connection.prepareStatement(sql)

    prepareStatement.use { statement ->
        params.forEach { p ->
            p.forEachIndexed { i, v -> statement.setObject(i + 1, v) }
            statement.addBatch()
        }

        affectedRows = statement.executeBatch().sum()
        if (generatedKeys.isNotEmpty()) {
            statement.generatedKeys?.let { rs ->
                generatedKeysResult = rs.use {
                    resultSetToMap(rs)
                }
            }
        }
    }

    return InsertResult(
        affectedRows,
        generatedKeysResult
    )
}

fun resultSetToMap(rs: ResultSet): MutableList<Map<String, Any>> {
    val list = mutableListOf<Map<String, Any>>()
    val md = rs.metaData
    while (rs.next()) {
        val map = mutableMapOf<String, Any>()
        (1..md.columnCount).forEach { i ->
            rs.getObject(i)?.let { value ->
                map[md.getColumnLabel(i)] = value
            }
        }
        list += map
    }
    return list
}

fun <T> SelectBuilder.execute(connection: Connection, mapper: (rs: ResultSet) -> T) = execute(connection, buildSqlAndParams(), mapper)
fun <T> SelectBuilder.execute(dataSource: DataSource, mapper: (rs: ResultSet) -> T) = dataSource.connection.use { execute(it, mapper) }

fun UpdateBuilder.execute(connection: Connection) = executeUpdate(connection, buildSqlAndParams())
fun UpdateBuilder.execute(dataSource: DataSource) = dataSource.connection.use { execute(it) }

fun DeleteBuilder.execute(connection: Connection) = executeUpdate(connection, buildSqlAndParams())
fun DeleteBuilder.execute(dataSource: DataSource) = dataSource.connection.use { execute(it) }

fun InsertBuilder.execute(connection: Connection) = executeBatch(connection, buildSqlAndParams(), generatedKeys)
fun InsertBuilder.execute(dataSource: DataSource) = dataSource.connection.use { execute(it) }

fun <T> QueryBuilder.execute(connection: Connection, mapper: (rs: ResultSet) -> T) = execute(connection, buildSqlAndParams(), mapper)
fun <T> QueryBuilder.execute(dataSource: DataSource, mapper: (rs: ResultSet) -> T) = dataSource.connection.use { execute(it, mapper) }
fun QueryBuilder.executeUpdate(connection: Connection) = executeUpdate(connection, buildSqlAndParams())
fun QueryBuilder.executeUpdate(dataSource: DataSource) = dataSource.connection.use { executeUpdate(it) }


