package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.*
import io.smallrye.mutiny.Uni
import io.vertx.core.impl.NoStackTraceThrowable
import io.vertx.mutiny.sqlclient.Pool
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.Tuple

private fun execute(pool: Pool, sqlAndParams: Pair<String, List<Any?>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams
    return pool
        .preparedQuery(sql)
        .execute(Tuple.tuple(params))
        .onFailure(NoStackTraceThrowable::class.java).transform { KodbqException(it) }
}

private fun executeInsert(pool: Pool, sqlAndParams: Pair<String, List<List<Any?>>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams
    return pool
        .preparedQuery(sql)
        .executeBatch(params.map { Tuple.tuple(it) })
        .onFailure(NoStackTraceThrowable::class.java).transform { KodbqException(it) }
}

private fun paramPlaceholder(index: Int) = "?"

fun SqlBuilder.execute(client: Pool) = execute(client, buildSqlAndParams(::paramPlaceholder))
fun InsertBuilder.execute(client: Pool) = executeInsert(client, buildSqlAndParams(::paramPlaceholder))