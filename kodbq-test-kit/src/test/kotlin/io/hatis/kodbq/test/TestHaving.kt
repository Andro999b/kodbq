package io.hatis.kodbq.test

import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.Table
import io.hatis.kodbq.kodbqDialect
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestHaving: StringSpec({
    beforeTest { kodbqDialect = SqlDialect.SQL92 }

    class TestTable: Table("table") {
        val col = column("col")
        val col1 = column("col1")
        val col2 = column("col2")
    }
    val table = TestTable()

    "should not add having without group by" {
        sqlSelect(table) {
            having { table.col eq 1 }
        }
            .expectSqlAndParams("select * from \"table\"")
    }
    "select having column EQ op" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                table.col1 eq value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                    "group by \"table\".\"col1\" " +
                    "having \"table\".\"col1\"=?",
                listOf(value)
            )
    }
    "select having column with or condition" {
        val value1 = 1
        val value2 = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                table.col1 eq value1
                or {
                    table.col1 eq value2
                }
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having \"table\".\"col1\"=? or \"table\".\"col1\"=?",
                listOf(value1, value2)
            )
    }
    "select having column with sum" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                sum(table.col2) gt value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having sum(\"table\".\"col2\")>?",
                listOf(value)
            )
    }
    "select having column with avg" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                avg(table.col2) gt value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having avg(\"table\".\"col2\")>?",
                listOf(value)
            )
    }
    "select having column with min" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                min(table.col2) gt value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having min(\"table\".\"col2\")>?",
                listOf(value)
            )
    }
    "select having column with max" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                max(table.col2) gt value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having max(\"table\".\"col2\")>?",
                listOf(value)
            )
    }
    "select having column with count" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                count() gt value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having count(*)>?",
                listOf(value)
            )
    }
    "select having column with count by column" {
        val value = 1
        sqlSelect(table) {
            groupBy(table.col1)
            having {
                count(table.col1) gt value
            }
        }
            .expectSqlAndParams(
                "select \"table\".\"col1\" from \"table\" " +
                        "group by \"table\".\"col1\" " +
                        "having count(\"table\".\"col1\")>?",
                listOf(value)
            )
    }
})