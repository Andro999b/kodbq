package io.hatis.kodbq.test

import io.hatis.kodbq.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe


class TestNativeSql : StringSpec({
    beforeTest { kodbqDialect=SqlDialect.MY_SQL }

    class T1: Table("t1") {
        val id = column("id")
        val c = column("c")
        val c1 = column("c1")
        val c2 = column("c2")
    }
    val t1 = T1()

    class T2: Table("t2") {
        val c = column("c")
        val t1id = column("t1_id")
    }
    val t2 = T2()

    "build native query" {
        val value = 1
        sql {
            """
                select * from ${t(t1)} where ${c(t1.c)} = ${v(value)}
                union
                select * from ${table(t2)} where ${column(t2.c)} = ${value(value)}
            """.trimIndent()
        }
            .expectSqlAndParams(
                """
                select * from `t1` where `t1`.`c` = ?
                union
                select * from `t2` where `t2`.`c` = ?
                """.trimIndent(),
                listOf(value, value)
            )
    }
    "insert native sql to in return section" {
        sqlSelect(t1) {
            returns { native("test") { "count(*)" } }
        }
            .expectSqlAndParams("select count(*) as test from `t1`")
    }
    "insert native sql to where section" {
        sqlSelect(t1) {
            t2.t1id joinOn t1.id
            where {
                native {
                    "${c(t1.c1)}=${column(t1.c2)}"
                }
            }
        }
            .expectSqlAndParams(
                "select * from `t1` " +
                        "join `t2` on `t2`.`t1_id`=`t1`.`id` " +
                        "where `t1`.`c1`=`t1`.`c2`"
            )
    }
    "insert native sql to insert value" {
        sqlInsert(t1) {
            values {
                native(t1.c) {
                    usage shouldBe NativeSqlColumn.Usage.INSERT
                    "now()"
                }
            }
        }
            .expectSqlAndParams("insert into `t1`(`c`) values(now())", listOf(emptyList<Any>()))
    }
    "insert native sql to update" {
        sqlUpdate(t1) {
            set {
                native(t1.c) {
                    usage shouldBe NativeSqlColumn.Usage.UPDATE
                    "now()"
                }
            }
        }
            .expectSqlAndParams("update `t1` set `c`=now()")
    }
})