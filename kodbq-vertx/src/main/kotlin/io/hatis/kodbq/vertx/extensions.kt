package io.hatis.kodbq.vertx

import io.hatis.kodbq.InsertBuilder
import io.hatis.kodbq.SqlBuilder
import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.defaultBuildOptions
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

fun execute(sqlClient: SqlClient, sqlAndParams: Pair<String, List<Any?>>, handler: Handler<AsyncResult<RowSet<Row>>>) {
    val (sql, params) = sqlAndParams
    sqlClient
        .preparedQuery(sql)
        .execute(Tuple.tuple(params), handler)
}

fun execute(sqlClient: SqlClient, sqlAndParams: Pair<String, List<Any?>>): Future<RowSet<Row>> {
    val (sql, params) = sqlAndParams
    return sqlClient
        .preparedQuery(sql)
        .execute(Tuple.tuple(params))
}

fun executeBatch(sqlClient: SqlClient, sqlAndParams: Pair<String, List<List<Any?>>>, handler: Handler<AsyncResult<RowSet<Row>>>) {
    val (sql, params) = sqlAndParams
    sqlClient
        .preparedQuery(sql)
        .executeBatch(params.map { Tuple.tuple(it) }, handler)
}

fun executeBatch(sqlClient: SqlClient, sqlAndParams: Pair<String, List<List<Any?>>>): Future<RowSet<Row>> {
    val (sql, params) = sqlAndParams
    return sqlClient
        .preparedQuery(sql)
        .executeBatch(params.map { Tuple.tuple(it) })
}

private val pgBuilderOptions = defaultBuildOptions.copy(expandIn = false, paramPlaceholder = { "\$$it" })
private val mssqlBuilderOptions = defaultBuildOptions.copy(paramPlaceholder = { "@p$it" })

private fun SqlBuilder.setBuilderOptionsByDialect() {
    if (dialect == SqlDialect.PG) buildOptions = pgBuilderOptions
    else if (dialect == SqlDialect.MS_SQL) buildOptions = mssqlBuilderOptions
}

fun SqlBuilder.execute(sqlClient: SqlClient, handler: Handler<AsyncResult<RowSet<Row>>>) {
    setBuilderOptionsByDialect()
    return execute(sqlClient, buildSqlAndParams(), handler)
}
fun SqlBuilder.execute(sqlClient: SqlClient): Future<RowSet<Row>> {
    setBuilderOptionsByDialect()
    return execute(sqlClient, buildSqlAndParams())
}
fun InsertBuilder.execute(sqlClient: SqlClient, handler: Handler<AsyncResult<RowSet<Row>>>) {
    setBuilderOptionsByDialect()
    executeBatch(sqlClient, buildSqlAndParams(), handler)
}
fun InsertBuilder.execute(sqlClient: SqlClient): Future<RowSet<Row>> {
    setBuilderOptionsByDialect()
    return executeBatch(sqlClient, buildSqlAndParams())
}
