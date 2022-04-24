package io.hatis.kodbq.quarkus

import io.hatis.kodbq.DbUtilsException
import io.hatis.kodbq.InsertBuilder
import io.hatis.kodbq.SqlBuilder
import io.smallrye.mutiny.Uni
import io.vertx.core.impl.NoStackTraceThrowable
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.SqlClient
import io.vertx.mutiny.sqlclient.Tuple
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("database-utils")

private fun execute(client: SqlClient, sqlAndParams: Pair<String, List<Any?>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams

    if(log.isDebugEnabled) {
        log.debug("$sql $params")
    }

    return client
        .preparedQuery(sql)
        .execute(Tuple.tuple(params))
        .onFailure(NoStackTraceThrowable::class.java).transform { DbUtilsException(it) }
}

private fun executeInsert(client: SqlClient, sqlAndParams: Pair<String, List<List<Any?>>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams

    if(log.isDebugEnabled) {
        log.debug("$sql $params")
    }

    return client
        .preparedQuery(sql)
        .executeBatch(params.map { Tuple.tuple(it) })
        .onFailure(NoStackTraceThrowable::class.java).transform { DbUtilsException(it) }
}

private fun paramPlaceholder(index: Int) = "$$index"

fun SqlBuilder.execute(client: SqlClient) = execute(client, buildSqlAndParams(::paramPlaceholder))
fun InsertBuilder.execute(client: SqlClient) = executeInsert(client, buildSqlAndParams(::paramPlaceholder))