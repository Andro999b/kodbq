package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.*
import io.r2dbc.spi.Statement
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


private fun batch(
    databaseClient: DatabaseClient,
    sqlAndParams: Pair<String, List<List<Any?>>>,
    generatedKeys: Set<String>,
    dialect: SqlDialect,
): Mono<InsertResult> {
    val (sql, params) = sqlAndParams

    return databaseClient.inConnection { connection ->
        var statement = connection.createStatement(sql)

        if (generatedKeys.isNotEmpty()) {
            statement = statement.returnGeneratedValues(*generatedKeys.toTypedArray())
        }

        params.forEach { statement = statement.bindParams(it).add() }

        //R2DBC suck with batch
        Flux.from(statement.execute())
            .flatMap {
                it.map { row, _ ->
                    val m = mutableMapOf<String, Any>()
                    generatedKeys.forEach { key ->
                        val value = row.get(key)
                        if (value != null) m[key] = value
                    }
                    return@map m
                }
            }
            .collectList()
            .map { InsertResult(it.size, it) }
    }
}

private fun query(
    databaseClient: DatabaseClient,
    sqlAndParams: Pair<String, List<Any?>>
): FetchSpec<MutableMap<String, Any>> {
    val (sql, params) = sqlAndParams

    var executeSpec = databaseClient.sql(sql)

    params.forEachIndexed { i, v ->
        executeSpec = if (v == null) {
            executeSpec.bindNull(i, Any::class.java)
        } else {
            executeSpec.bind(i, v)
        }
    }

    return executeSpec.fetch()
}

private fun Statement.bindParams(params: List<Any?>): Statement {
    params.forEachIndexed { i, v ->
        if (v == null) {
            bindNull(i, Any::class.java)
        } else {
            bind(i, v)
        }
    }

    return this
}

private val r2dbcBuildOptions = defaultBuildOptions.copy(
    generatedKeysSql = false,
    paramPlaceholder = { "\$$it" }
)

fun SqlBuilder.execute(databaseClient: DatabaseClient): FetchSpec<MutableMap<String, Any>> {
    buildOptions = r2dbcBuildOptions
    return query(databaseClient, buildSqlAndParams())
}

fun InsertBuilder.execute(databaseClient: DatabaseClient): Mono<InsertResult> {
    buildOptions = r2dbcBuildOptions
    return batch(
        databaseClient,
        buildSqlAndParams(),
        generatedKeys,
        dialect
    )
}
