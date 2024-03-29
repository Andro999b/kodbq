package io.hatis.kodbq.test

import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.Table
import io.hatis.kodbq.WhereOps
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestWhereOps : StringSpec({
    val table = Table("table")
    val col = table.column("col")

    "select form table with op: LT" {
        val value = 0
        sqlSelect(table) {
            where { col lt value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"<?",
                listOf(value)
            )
    }
    "select form table with op: GT" {
        val value = 0
        sqlSelect(table) {
            where { col gt value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\">?",
                listOf(value)
            )
    }
    "select form table with op: LTE" {
        val value = 0
        sqlSelect(table) {
            where { col lte value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"<=?",
                listOf(value)
            )
    }
    "select form table with op: GTE" {
        val value = 0
        sqlSelect(table) {
            where { col gte value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\">=?",
                listOf(value)
            )
    }
    "select form table with op: EQ" {
        val value = 0
        sqlSelect(table) {
            where { col eq value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"=?",
                listOf(value)
            )
    }
    "select form table with op: NEQ" {
        val value = 0
        sqlSelect(table) {
            where { col neq value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"!=?",
                listOf(value)
            )
    }
    "select form table with op: LIKE" {
        val value = "test"
        sqlSelect(table) {
            where { col like value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\" like ?",
                listOf(value)
            )
    }
    "select form table with op: IN for postgres" {
        val value = arrayOf(0, 1, 2)
        sqlSelect(table) {
            where { col `in` value }
        }
            .apply {
                buildOptions = buildOptions.copy(expandIn = false)
            }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"=any(?)",
                listOf(value)
            )
    }
    "select form table with op: IN for mysql" {
        val value = arrayOf(0, 1, 2)
        sqlSelect(table, dialect = SqlDialect.MY_SQL) {
            where { col `in` value }
        }
            .expectSqlAndParams(
                "select * from `table` where `table`.`col` in(?,?,?)",
                value.toList()
            )
    }
    "select from table where col is null" {
        sqlSelect(table, dialect = SqlDialect.MY_SQL) {
            where { col.isNull() }
        }
            .expectSqlAndParams("select * from `table` where `table`.`col` is null")
    }
    "select from table where col is not null" {
        sqlSelect(table, dialect = SqlDialect.MY_SQL) {
            where { col.notNull() }
        }
            .expectSqlAndParams("select * from `table` where `table`.`col` is not null")
    }
    "select from table with native sql condition" {
        val name = "bob"
        sqlSelect(table, dialect = SqlDialect.MS_SQL) {
            where {
                native {
                    "json_value(${c(col)}, '$.name')=${v(name)}"
                }
            }
        }
            .expectSqlAndParams("select * from [table] where json_value([table].[col], '\$.name')=?", listOf(name))
    }
    "select with column condition api: eq" {
        val value = "string"
        sqlSelect(table) {
            where { col eq value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"=?",
                listOf(value)
            )
    }
    "select with column condition api: neq" {
        val value = "string"
        sqlSelect(table) {
            where { col neq value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"!=?",
                listOf(value)
            )
    }
    "select with column condition api: lt" {
        val value = 10
        sqlSelect(table) {
            where { col lt value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"<?",
                listOf(value)
            )
    }
    "select with column condition api: gt" {
        val value = 10
        sqlSelect(table) {
            where { col gt value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\">?",
                listOf(value)
            )
    }
    "select with column condition api: lte" {
        val value = 10
        sqlSelect(table) {
            where { col lte value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\"<=?",
                listOf(value)
            )
    }
    "select with column condition api: gte" {
        val value = 10
        sqlSelect(table) {
            where { col gte value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\">=?",
                listOf(value)
            )
    }
    "select with column condition api: like" {
        val value = "str1"
        sqlSelect(table) {
            where { col like value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\" like ?",
                listOf(value)
            )
    }
    "select with column condition api: in" {
        val value = setOf("str1", "str2")
        sqlSelect(table) {
            where { col `in` value }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\" in(?,?)",
                value.toList()
            )
    }
    "select with column condition api: is null" {
        sqlSelect(table) {
            where { col.isNull() }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\" is null"
            )
    }
    "select with column condition api: not null" {
        sqlSelect(table) {
            where { col.notNull() }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\" is not null"
            )
    }
})