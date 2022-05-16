package io.hatis.kodbq.spring.r2dbc

import io.hatis.kodbq.*
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux

private fun buildSpec(
    databaseClient: DatabaseClient,
    sqlAndParams: Pair<String, List<Any?>>
): DatabaseClient.GenericExecuteSpec {
    val (sql, params) = sqlAndParams

    var executeSpec = databaseClient.sql(sql)

    params.forEachIndexed { i, v ->
        executeSpec = if (v == null) {
            executeSpec.bindNull(i, Any::class.java)
        } else {
            executeSpec.bind(i, v)
        }
    }

    return executeSpec
}

private val r2dbcMSSqlBuildOptions = defaultBuildOptions.copy(paramPlaceholder = {"@p${it-1}"})
private val r2dbcPGBuildOptions = defaultBuildOptions.copy(paramPlaceholder = {"\$$it"})

private fun SqlBuilder.setBuildOptionsForDialect() {
    if (dialect == SqlDialect.MS_SQL) buildOptions = r2dbcMSSqlBuildOptions
    else if (dialect == SqlDialect.PG) buildOptions = r2dbcPGBuildOptions
}

fun SqlBuilder.build(databaseClient: DatabaseClient): DatabaseClient.GenericExecuteSpec {
    setBuildOptionsForDialect()
    return buildSpec(databaseClient, buildSqlAndParams())
}

fun InsertBuilder.build(databaseClient: DatabaseClient): Flux<DatabaseClient.GenericExecuteSpec> {
    setBuildOptionsForDialect()
    val (sql, params) = buildSqlAndParams()

    return Flux.fromIterable(params)
        .map { buildSpec(databaseClient, sql to it) }
}