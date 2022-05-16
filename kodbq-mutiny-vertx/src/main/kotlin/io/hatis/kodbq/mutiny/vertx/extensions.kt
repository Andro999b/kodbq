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

private val pgBuilderOptions = defaultBuildOptions.copy(expandIn = false, paramPlaceholder = { "\$$it" })
private val mssqlBuilderOptions = defaultBuildOptions.copy(paramPlaceholder = { "@p$it" })

private fun SqlBuilder.setBuilderOptionsByDialect() {
    if (dialect == SqlDialect.PG) buildOptions = pgBuilderOptions
    else if (dialect == SqlDialect.MS_SQL) buildOptions = mssqlBuilderOptions
}

fun SqlBuilder.execute(client: SqlClient): Uni<RowSet<Row>> {
    setBuilderOptionsByDialect()
    return execute(client, buildSqlAndParams())
}
fun InsertBuilder.execute(client: SqlClient): Uni<RowSet<Row>> {
    setBuilderOptionsByDialect()
    return executeInsert(client, buildSqlAndParams())
}