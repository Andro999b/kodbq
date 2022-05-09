package io.hatis.kodbq.fluentjdbc

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.apache.commons.dbcp2.BasicDataSource
import org.codejargon.fluentjdbc.api.FluentJdbcBuilder
import org.codejargon.fluentjdbc.api.mapper.Mappers

abstract class FluentJdbcTest(url: String, sqlDialect: SqlDialect): StringSpec({
    val basicDataSource = BasicDataSource()
    basicDataSource.url = url
    basicDataSource.initialSize = 1

    val fluentJdbc = FluentJdbcBuilder()
        .connectionProvider(basicDataSource)
        .build()

    val execute: ExecuteAndGetFun = {
        when (this) {
            is SelectBuilder -> build(fluentJdbc).listResult(Mappers.map())
            is InsertBuilder -> {
                val affectedRows = if (generatedKeys.isEmpty()) {
                    execute(fluentJdbc).sumOf { it.affectedRows() }.toInt()
                } else {
                    val result = build(fluentJdbc).runFetchGenKeys(Mappers.map())

                    result.forAtLeastOne {
                        it.generatedKeys().size shouldBe generatedKeys.size
                    }

                    result.sumOf { it.affectedRows() }.toInt()
                }
                listOf(mapOf("affectedRows" to affectedRows))
            }
            is UpdateBuilder -> {
                listOf(mapOf("affectedRows" to execute(fluentJdbc).affectedRows()))
            }
            is DeleteBuilder -> {
                listOf(mapOf("affectedRows" to execute(fluentJdbc).affectedRows()))
            }
            else -> throw IllegalStateException("Not supported builder")
        }
    }

    beforeTest { kodbqDialect = sqlDialect }

    include(selectsTestFactory(execute))
    include(updateTestFactory(execute))
})