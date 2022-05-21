package io.hatis.kodbq.test

import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestUnions: StringSpec({
    "select with union in same table" {
        sqlSelect("table") {
            where { id(1) }
            union { where { id(2) } }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"id\"=?\n" +
                        "union\n" +
                        "select * from \"table\" where \"table\".\"id\"=?",
                listOf(1, 2)
            )
    }
    "select with union all" {
        sqlSelect("table") {
            where { id(1) }
            union(all = true) { where { id(2) } }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"id\"=?\n" +
                        "union all\n" +
                        "select * from \"table\" where \"table\".\"id\"=?",
                listOf(1, 2)
            )
    }
    "select with union with different table" {
        sqlSelect("table1") {
            where { id(1) }
            union("table2", all = true) { where { id(2) } }
        }
            .expectSqlAndParams(
                "select * from \"table1\" where \"table1\".\"id\"=?\n" +
                        "union all\n" +
                        "select * from \"table2\" where \"table2\".\"id\"=?",
                listOf(1, 2)
            )
    }
})