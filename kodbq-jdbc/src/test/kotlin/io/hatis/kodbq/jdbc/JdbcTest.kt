package io.hatis.kodbq.jdbc

import io.hatis.kodbq.*
import io.hatis.kodbq.test.ExecuteAndGetFun
import io.hatis.kodbq.test.selectsTestFactory
import io.hatis.kodbq.test.updateTestFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.shouldBe
import org.apache.commons.dbcp2.BasicDataSource

abstract class JdbcTest(url: String, sqlDialect: SqlDialect): StringSpec({
    val dataSource = BasicDataSource()
    dataSource.url = url
    dataSource.initialSize = 1

    val execute: ExecuteAndGetFun = {
        when (this) {
            is SelectBuilder -> execute(dataSource, ::resultSetToMap)
            is InsertBuilder -> {
                val result = execute(dataSource)
                if(generatedKeys.isNotEmpty()) {
                    result.generatedKeys.forAtLeastOne {
                        it.keys.size shouldBe generatedKeys.size
                    }
                }
                listOf(mapOf("affectedRows" to result.affectedRows))
            }
            is UpdateBuilder -> {
                listOf(mapOf("affectedRows" to execute(dataSource)))
            }
            is DeleteBuilder -> {
                listOf(mapOf("affectedRows" to execute(dataSource)))
            }
            else -> throw IllegalStateException("Not supported builder")
        }
    }

    beforeTest { kodbqDialect = sqlDialect }

    include(selectsTestFactory(execute))
    include(updateTestFactory(execute))
})