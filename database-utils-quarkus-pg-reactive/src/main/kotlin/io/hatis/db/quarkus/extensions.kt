package io.hatis.db.quarkus

import io.hatis.db.DeleteBuilder
import io.hatis.db.InsertBuilder
import io.hatis.db.SelectBuilder
import io.hatis.db.UpdateBuilder
import io.hatis.db.pg.buildSqlAndParams
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.SqlClient
import io.vertx.mutiny.sqlclient.Tuple

private fun execute(client:  SqlClient, sqlAndParams: Pair<String, List<Any?>>): Uni<RowSet<Row>> {
    val (sql, params) = sqlAndParams

    return client
        .preparedQuery(sql)
        .execute(Tuple.tuple(params))
}

private fun paramPlaceholder(index: Int) = "$$index"

fun UpdateBuilder.execute(client:  SqlClient) = execute(client, buildSqlAndParams(::paramPlaceholder))
fun InsertBuilder.execute(client:  SqlClient) = execute(client, buildSqlAndParams())
fun SelectBuilder.execute(client:  SqlClient) = execute(client, buildSqlAndParams(::paramPlaceholder))
fun DeleteBuilder.execute(client:  SqlClient) = execute(client, buildSqlAndParams(::paramPlaceholder))