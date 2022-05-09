package io.hatis.kodbq.test

import io.hatis.kodbq.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestNativeSql : StringSpec({
    beforeTest { kodbqDialect=SqlDialect.MY_SQL }
    "build native query" {
        val value = 1
        sql {
            """
                select * from ${t("t1")} where ${c("c")} = ${v(value)}
                union
                select * from ${table("t2")} where ${column("c")} = ${value(value)}
                union 
                select * from ${t("t3")} where ${c("t3","c")} = ${v(value)}
                union 
                select * from ${table("t4")} where ${column("t4", "c")} = ${value(value)}
            """.trimIndent()
        }
            .expectSqlAndParams(
                """
                select * from `t1` where `c` = ?
                union
                select * from `t2` where `c` = ?
                union 
                select * from `t3` where `t3`.`c` = ?
                union 
                select * from `t4` where `t4`.`c` = ?
                """.trimIndent(),
                listOf(value, value, value, value)
            )
    }
    "insert native sql to in return section" {
        sqlSelect("t1") {
            returns { native("test") { "count(*)" } }
        }
            .expectSqlAndParams("select count(*) as test from `t1`")
    }
    "insert native sql to where section" {
        sqlSelect("t1") {
            join("t2", "t1_id") on "id"
            where {
                native("c1") {
                    usage shouldBe NativeSqlColumn.Usage.CONDITION
                    "$c=${column("c2")}"
                }
                native("c3") {
                    usage shouldBe NativeSqlColumn.Usage.CONDITION
                    "$c=${column("t2", "c2")}"
                }
                native("c4") {
                    usage shouldBe NativeSqlColumn.Usage.CONDITION
                    "$c=${c("c2")}"
                }
                native("c5") {
                    usage shouldBe NativeSqlColumn.Usage.CONDITION
                    "$c=${c("t2", "c2")}"
                }
            }
        }
            .expectSqlAndParams(
                "select * from `t1` " +
                        "join `t2` on `t2`.`t1_id`=`t1`.`id` " +
                        "where `t1`.`c1`=`t1`.`c2` " +
                        "and `t1`.`c3`=`t2`.`c2` " +
                        "and `t1`.`c4`=`t1`.`c2` " +
                        "and `t1`.`c5`=`t2`.`c2`"
            )
    }
    "insert native sql to insert value" {
        sqlInsert("t1") {
            values {
                native("col") {
                    usage shouldBe NativeSqlColumn.Usage.INSERT
                    "now()"
                }
            }
        }
            .expectSqlAndParams("insert into `t1`(`col`) values(now())", listOf(emptyList<Any>()))
    }
    "insert native sql to update" {
        sqlUpdate("t1") {
            set {
                native("col") {
                    usage shouldBe NativeSqlColumn.Usage.UPDATE
                    "now()"
                }
            }
        }
            .expectSqlAndParams("update `t1` set `col`=now()")
    }
})