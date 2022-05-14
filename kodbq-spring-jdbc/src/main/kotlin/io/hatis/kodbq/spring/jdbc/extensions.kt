package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.*
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCallback
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.RowMapperResultSetExtractor
import org.springframework.jdbc.support.JdbcUtils
import java.sql.Statement

private fun update(template: JdbcTemplate, sqlAndParams: Pair<String, List<Any?>>): Int {
    val (sql, params) = sqlAndParams

    return template.update(sql, *params.toTypedArray())
}

private fun insert(
    template: JdbcTemplate,
    sqlAndParams: Pair<String, List<List<Any?>>>,
    generatedKeys: Set<String>
): InsertResult {
    val (sql, params) = sqlAndParams

    var generatedKeysResult: List<Map<String, Any>> = emptyList()
    var affectedRows = 0
    template.execute({
        val ps = if (generatedKeys.isEmpty())
            it.prepareStatement(sql)
        else
            it.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

        params.forEach { rowParam ->
            rowParam.forEachIndexed { i, v ->
                ps.setObject(i + 1, v)
            }
            ps.addBatch()
        }

        ps
    }, PreparedStatementCallback { ps ->
        affectedRows = ps.executeBatch().sum()
        if(generatedKeys.isNotEmpty()) {
            ps.generatedKeys?.let { rs ->
                try {
                    generatedKeysResult = RowMapperResultSetExtractor(ColumnMapRowMapper()).extractData(rs)
                } finally {
                    JdbcUtils.closeResultSet(rs)
                }
            }
        }
    })

    return InsertResult(
        affectedRows,
        generatedKeysResult
    )
}

private fun <T> query(template: JdbcTemplate, mapper: RowMapper<T>, sqlAndParams: Pair<String, List<Any?>>): MutableList<T> {
    val (sql, params) = sqlAndParams

    return template.query(sql, mapper, *params.toTypedArray())
}

fun UpdateBuilder.execute(template: JdbcTemplate) = update(template, buildSqlAndParams())
fun InsertBuilder.execute(template: JdbcTemplate) = insert(template, buildSqlAndParams(), generatedKeys)

fun <T> SelectBuilder.execute(template: JdbcTemplate, mapper: RowMapper<T>) = query(template, mapper, buildSqlAndParams())
fun DeleteBuilder.execute(template: JdbcTemplate) = update(template, buildSqlAndParams())
fun QueryBuilder.executeUpdate(template: JdbcTemplate) = update(template, buildSqlAndParams())
fun <T> QueryBuilder.execute(template: JdbcTemplate, mapper: RowMapper<T>) = query(template, mapper, buildSqlAndParams())
