package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.*
import io.r2dbc.spi.Result
import io.r2dbc.spi.Statement
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


private fun batch(databaseClient: DatabaseClient, sqlAndParams: Pair<String, List<List<Any?>>>): Flux<Result> {
    val (sql, params) = sqlAndParams

    return databaseClient.inConnectionMany { connection ->
        var statement = connection.createStatement(sql)
        params.forEach { statement = statement.bindParams(it).add() }
        Flux.from(statement.execute())
    }
}

private fun query(databaseClient: DatabaseClient, sqlAndParams: Pair<String, List<Any?>>): Mono<Result> {
    val (sql, params) = sqlAndParams

    return databaseClient.inConnection {
        Mono.from(it.createStatement(sql).bindParams(params).execute())
    }
}

private fun Statement.bindParams(params: List<Any?>): Statement {
    params.forEachIndexed { i, v ->
        if(v == null) {
            bindNull(paramName(i), Any::class.java)
        } else {
            bind(paramName(i), v)
        }
    }

    return this
}

private fun paramName(index: Int) = "p$index"
private fun paramPlaceholder(index: Int) = ":${paramName(index)}"

fun UpdateBuilder.execute(databaseClient: DatabaseClient) = query(databaseClient, buildSqlAndParams(::paramPlaceholder))
fun InsertBuilder.execute(databaseClient: DatabaseClient) = batch(databaseClient, buildSqlAndParams(::paramPlaceholder))
fun SelectBuilder.execute(databaseClient: DatabaseClient) = query(databaseClient, buildSqlAndParams(::paramPlaceholder))
fun DeleteBuilder.execute(databaseClient: DatabaseClient) = query(databaseClient, buildSqlAndParams(::paramPlaceholder))
fun QueryBuilder.execute(databaseClient: DatabaseClient) = query(databaseClient, buildSqlAndParams(::paramPlaceholder))
