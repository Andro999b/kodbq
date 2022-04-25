import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestJoins: StringSpec({
    "select with inner join" {
        sqlSelect("t1", SqlDialect.MY_SQL) {
            join("t2", "t1_id") on "id"
        }
            .expectSqlAndParams("select * from `t1` join `t2` on `t2`.`t1_id` = `t1`.`id`")
    }
    "select with left join" {
        sqlSelect("t1", SqlDialect.MY_SQL) {
            leftJoin("t2", "t1_id") on "id"
        }
            .expectSqlAndParams("select * from `t1` left join `t2` on `t2`.`t1_id` = `t1`.`id`")
    }
    "select with right join" {
        sqlSelect("t1", SqlDialect.MY_SQL) {
            rightJoin("t2", "t1_id") on "id"
        }
            .expectSqlAndParams("select * from `t1` right join `t2` on `t2`.`t1_id` = `t1`.`id`")
    }
    "select with full join" {
        sqlSelect("t1", SqlDialect.MY_SQL) {
            fullJoin("t2", "t1_id") on "id"
        }
            .expectSqlAndParams("select * from `t1` full join `t2` on `t2`.`t1_id` = `t1`.`id`")
    }
    "select with multiple joins" {
        sqlSelect("t1", SqlDialect.MY_SQL) {
            join("t2", "t1_id") on "id"
            leftJoin("t3", "t2_id").on("t1","id")
        }
            .expectSqlAndParams("select * from `t1` join `t2` on `t2`.`t1_id` = `t1`.`id` left join `t3` on `t3`.`t2_id` = `t1`.`id`")
    }
})