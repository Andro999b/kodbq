package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

private fun update(template: JdbcTemplate, sqlAndParams: Pair<String, List<Any?>>): Int {
    val (sql, params) = sqlAndParams

    return template.update(sql, *params.toTypedArray())
}

private fun updateBatch(template: JdbcTemplate, sqlAndParams: Pair<String, List<List<Any?>>>): IntArray {
    val (sql, params) = sqlAndParams

    return template.batchUpdate(sql, params.map { it.toTypedArray() })
}

private fun <T> query(template: JdbcTemplate, mapper: RowMapper<T>, sqlAndParams: Pair<String, List<Any?>>): MutableList<T> {
    val (sql, params) = sqlAndParams

    return template.query(sql, mapper, *params.toTypedArray())
}

@Suppress("UNUSED_PARAMETER")
private fun paramPlaceholder(index: Int) = "?"

fun UpdateBuilder.execute(template: JdbcTemplate) = update(template, buildSqlAndParams(::paramPlaceholder))
fun InsertBuilder.execute(template: JdbcTemplate) = updateBatch(template, buildSqlAndParams(::paramPlaceholder))
fun <T> SelectBuilder.execute(template: JdbcTemplate, mapper: RowMapper<T>) = query(template, mapper, buildSqlAndParams(::paramPlaceholder))
fun DeleteBuilder.execute(template: JdbcTemplate) = update(template, buildSqlAndParams(::paramPlaceholder))
fun QueryBuilder.update(template: JdbcTemplate) = update(template, buildSqlAndParams(::paramPlaceholder))
fun <T> QueryBuilder.query(template: JdbcTemplate, mapper: RowMapper<T>) = query(template, mapper, buildSqlAndParams(::paramPlaceholder))
