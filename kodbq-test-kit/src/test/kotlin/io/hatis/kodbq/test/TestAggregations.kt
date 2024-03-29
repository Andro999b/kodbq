package io.hatis.kodbq.test

import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.Table
import io.hatis.kodbq.kodbqDialect
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestAggregations: StringSpec({
    beforeTest { kodbqDialect = SqlDialect.MY_SQL }

    val table = Table("table")
    val col = table.column("col")
    
    "select count" {
        sqlSelect(table) {
            returns { count("res") }
        }
            .expectSqlAndParams("select count(*) as res from `table`")
    }
    "select count by column" {
        sqlSelect(table) {
            returns { count(col, "res") }
        }
            .expectSqlAndParams("select count(`table`.`col`) as res from `table`")
    }
    "select sum" {
        sqlSelect(table) {
            returns { sum(col,"res") }
        }
            .expectSqlAndParams("select sum(`table`.`col`) as res from `table`")
    }
    "select avg" {
        sqlSelect(table) {
            returns { avg(col,"res") }
        }
            .expectSqlAndParams("select avg(`table`.`col`) as res from `table`")
    }
    "select min" {
        sqlSelect(table) {
            returns { min(col,"res") }
        }
            .expectSqlAndParams("select min(`table`.`col`) as res from `table`")
    }
    "select max" {
        sqlSelect(table) {
            returns { max(col,"res") }
        }
            .expectSqlAndParams("select max(`table`.`col`) as res from `table`")
    }
    "select mysql length function" {
        sqlSelect(table) {
            returns { function("length",col,"res") }
        }
            .expectSqlAndParams("select length(`table`.`col`) as res from `table`")
    }
    "select with native sql" {
        val prefix = "test_"
        sqlSelect(table) {
            returns {
                native("res") {
                    "concat(${v(prefix)}, ${c(col)})"
                }
            }
        }
            .expectSqlAndParams(
                "select concat(?, `table`.`col`) as res from `table`",
                listOf(prefix)
            )
    }
})