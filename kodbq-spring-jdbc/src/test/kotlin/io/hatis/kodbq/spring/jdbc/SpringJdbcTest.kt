package io.hatis.kodbq.spring.jdbc

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

abstract class SpringJdbcTest(url: String, sqlDialect: SqlDialect): StringSpec({
    val dataSource = SingleConnectionDataSource()
    dataSource.url = url

    val jdbcTemplate = JdbcTemplate(dataSource)
    val execute: ExecuteAndGetFun = executorFactory(jdbcTemplate)

    beforeTest { kodbqDialect = sqlDialect }

    include(selectsTestFactory(execute))
    include(updateTestFactory(execute))
})

fun executorFactory(jdbcTemplate: JdbcTemplate): ExecuteAndGetFun {
    val execute: ExecuteAndGetFun = {
        when (this) {
            is SelectBuilder -> execute(jdbcTemplate, ColumnMapRowMapper())
            is InsertBuilder -> {
                val result = execute(jdbcTemplate)
                if (dialect != SqlDialect.MS_SQL && generatedKeys.isNotEmpty()) {
                    result.generatedKeys.forAtLeastOne { map ->
                        should { map.keys.containsAll(generatedKeys) }
                    }
                }
                listOf(mapOf("affectedRows" to result.affectedRows))
            }
            is UpdateBuilder -> listOf(mapOf("affectedRows" to execute(jdbcTemplate)))
            is DeleteBuilder -> listOf(mapOf("affectedRows" to execute(jdbcTemplate)))
            else -> throw IllegalStateException("Not supported builder")
        }
    }
    return execute
}