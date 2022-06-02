package io.hatis.kodbq.test

import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.Table
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestJoins: StringSpec({
    class T1: Table("t1") {
        val id = column("id")
    }
    val t1 = T1()

    class T2: Table("t2") {
        val id = column("id")
        val t1id = column("t1_id")
        val t1ref = t1id refernce t1.id
    }
    val t2 = T2()

    class T3: Table("t3") {
        val t2id = column("t2_id")
        val t2ref = t2id refernce t2.id
    }
    val t3 = T3()


    "select with inner join" {
        sqlSelect(t1, SqlDialect.MY_SQL) {
            t2.t1id joinOn t1.id
        }
            .expectSqlAndParams("select * from `t1` join `t2` on `t2`.`t1_id`=`t1`.`id`")
    }
    "select with left join" {
        sqlSelect(t1, SqlDialect.MY_SQL) {
            t2.t1id leftJoinOn t1.id
        }
            .expectSqlAndParams("select * from `t1` left join `t2` on `t2`.`t1_id`=`t1`.`id`")
    }
    "select with right join" {
        sqlSelect(t1, SqlDialect.MY_SQL) {
            t2.t1id rightJoinOn t1.id
        }
            .expectSqlAndParams("select * from `t1` right join `t2` on `t2`.`t1_id`=`t1`.`id`")
    }
    "select with full join" {
        sqlSelect(t1, SqlDialect.MY_SQL) {
            fullJoin(t2.t1ref)
        }
            .expectSqlAndParams("select * from `t1` full join `t2` on `t2`.`t1_id`=`t1`.`id`")
    }
    "select with multiple joins" {
        sqlSelect(t1, SqlDialect.MY_SQL) {
            t2.t1id joinOn t1.id
            leftJoin(t3.t2ref)
        }
            .expectSqlAndParams("select * from `t1` join `t2` on `t2`.`t1_id`=`t1`.`id` left join `t3` on `t3`.`t2_id`=`t2`.`id`")
    }
})