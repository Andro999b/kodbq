package io.hatis.kodbq.test

import io.hatis.kodbq.Table
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestUnions: StringSpec({
    val table = Table("table")
    val id = table.column("id")

    "select with union in same table" {
        sqlSelect(table) {
            where { column(id, 1)}
            union { where { column(id, 2) } }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"id\"=?\n" +
                        "union\n" +
                        "select * from \"table\" where \"table\".\"id\"=?",
                listOf(1, 2)
            )
    }
    "select with union all" {
        sqlSelect(table) {
            where { column(id, 1)}
            union(all = true) { where { column(id, 2) } }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"id\"=?\n" +
                        "union all\n" +
                        "select * from \"table\" where \"table\".\"id\"=?",
                listOf(1, 2)
            )
    }
    "select with union with different table" {
        val table1 = Table("table1")
        val table2 = Table("table2")
        val id1 = table1.column("id")
        val id2 = table2.column("id")

        sqlSelect(table1) {
            where { column(id1, 1)}
            union(table2, all = true) { where { column(id2, 2) } }
        }
            .expectSqlAndParams(
                "select * from \"table1\" where \"table1\".\"id\"=?\n" +
                        "union all\n" +
                        "select * from \"table2\" where \"table2\".\"id\"=?",
                listOf(1, 2)
            )
    }
})