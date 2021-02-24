package io.hatis.db.quarkus

import io.hatis.db.DeleteBuilder
import io.hatis.db.InsertBuilder
import io.hatis.db.SelectBuilder
import io.hatis.db.UpdateBuilder
import io.hatis.db.pg.buildSqlAndParams
import org.codejargon.fluentjdbc.api.FluentJdbc
import org.codejargon.fluentjdbc.api.query.SelectQuery
import org.codejargon.fluentjdbc.api.query.UpdateQuery

private fun update(fluentJdbc: FluentJdbc, sqlAndParams: Pair<String, List<Any?>>): UpdateQuery {
    val (sql, params) = sqlAndParams

    return fluentJdbc.query()
        .update(sql)
        .params(params)
}

private fun select(fluentJdbc: FluentJdbc, sqlAndParams: Pair<String, List<Any?>>): SelectQuery {
    val (sql, params) = sqlAndParams

    return fluentJdbc.query()
        .select(sql)
        .params(params)
}

private fun paramPlaceholder(index: Int) = "?"

fun UpdateBuilder.build(fluentJdbc: FluentJdbc) = update(fluentJdbc, buildSqlAndParams(::paramPlaceholder))
fun InsertBuilder.build(fluentJdbc: FluentJdbc) = update(fluentJdbc, buildSqlAndParams())
fun SelectBuilder.build(fluentJdbc: FluentJdbc) = select(fluentJdbc, buildSqlAndParams(::paramPlaceholder))
fun DeleteBuilder.build(fluentJdbc: FluentJdbc) = update(fluentJdbc, buildSqlAndParams(::paramPlaceholder))