package io.hatis.kodbq.mutiny.vertx

import io.hatis.kodbq.*
import io.smallrye.mutiny.Uni
import io.vertx.core.impl.NoStackTraceThrowable
import io.vertx.mutiny.sqlclient.SqlClient
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.Tuple

private fun execute(sqlClient: SqlClient, sqlAndParams: Pair<String, List<Any?>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams
    return sqlClient
        .preparedQuery(sql)
        .execute(Tuple.tuple(params))
        .onFailure(NoStackTraceThrowable::class.java).transform { KodbqException(it) }
}

private fun executeInsert(sqlClient: SqlClient, sqlAndParams: Pair<String, List<List<Any?>>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams
    return sqlClient
        .preparedQuery(sql)
        .executeBatch(params.map { Tuple.tuple(it) })
        .onFailure(NoStackTraceThrowable::class.java).transform { KodbqException(it) }
}

fun SqlBuilder.execute(client: SqlClient) = execute(client, buildSqlAndParams())
fun InsertBuilder.execute(client: SqlClient) = executeInsert(client, buildSqlAndParams())