package io.hatis.kodbq.fluentjdbc

import io.hatis.kodbq.*
import org.codejargon.fluentjdbc.api.FluentJdbc
import org.codejargon.fluentjdbc.api.query.BatchQuery
import org.codejargon.fluentjdbc.api.query.SelectQuery
import org.codejargon.fluentjdbc.api.query.UpdateQuery
import org.codejargon.fluentjdbc.api.query.UpdateResult

private fun update(fluentJdbc: FluentJdbc, sqlAndParams: Pair<String, List<Any?>>): UpdateQuery {
    val (sql, params) = sqlAndParams

    return fluentJdbc.query()
        .update(sql)
        .params(params)
}

private fun batch(fluentJdbc: FluentJdbc, sqlAndParams: Pair<String, List<List<Any?>>>): BatchQuery {
    val (sql, params) = sqlAndParams

    return fluentJdbc.query()
        .batch(sql)
        .params(params)
}

private fun select(fluentJdbc: FluentJdbc, sqlAndParams: Pair<String, List<Any?>>): SelectQuery {
    val (sql, params) = sqlAndParams

    return fluentJdbc.query()
        .select(sql)
        .params(params)
}

fun UpdateBuilder.build(fluentJdbc: FluentJdbc) = update(fluentJdbc, buildSqlAndParams())
fun UpdateBuilder.execute(fluentJdbc: FluentJdbc): UpdateResult = build(fluentJdbc).run()
fun InsertBuilder.build(fluentJdbc: FluentJdbc): BatchQuery {
    buildOptions = buildOptions.copy(generatedKeysSql = false)
    return batch(fluentJdbc, buildSqlAndParams())
}
fun InsertBuilder.execute(fluentJdbc: FluentJdbc): Collection<UpdateResult> = build(fluentJdbc).run()
fun SelectBuilder.build(fluentJdbc: FluentJdbc) = select(fluentJdbc, buildSqlAndParams())
fun DeleteBuilder.build(fluentJdbc: FluentJdbc) = update(fluentJdbc, buildSqlAndParams())
fun DeleteBuilder.execute(fluentJdbc: FluentJdbc): UpdateResult = build(fluentJdbc).run()
fun QueryBuilder.build(fluentJdbc: FluentJdbc) = select(fluentJdbc, buildSqlAndParams())
fun QueryBuilder.buildUpdate(fluentJdbc: FluentJdbc) = update(fluentJdbc, buildSqlAndParams())
fun QueryBuilder.executeUpdate(fluentJdbc: FluentJdbc): UpdateResult = buildUpdate(fluentJdbc).run()
