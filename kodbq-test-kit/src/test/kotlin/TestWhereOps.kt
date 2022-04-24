import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.WhereOps
import io.hatis.kodbq.sqlSelect
import io.kotest.core.spec.style.StringSpec

class TestWhereOps : StringSpec({
    listOf(
        WhereOps.LT,
        WhereOps.GT,
        WhereOps.LTE,
        WhereOps.GTE,
        WhereOps.EQ,
        WhereOps.NEQ,
        WhereOps.LIKE
    ).forEach { op ->
        "select form table with op: $op" {
            val value = 0
            sqlSelect("table") {
                where { column("col", op, value) }
            }
                .expectSqlAndParams(
                    "select * from \"table\" where \"table\".\"col\" ${op.op} ?",
                    listOf(value)
                )
        }
    }
    "select form table with op: IN for postgres" {
        val value = arrayOf(0, 1, 2)
        sqlSelect("table") {
            where { column("col", WhereOps.IN, value) }
        }
            .expectSqlAndParams(
                "select * from \"table\" where \"table\".\"col\" = any(?)",
                listOf(value)
            )
    }
    "select form table with op: IN for mysql" {
        val value = arrayOf(0, 1, 2)
        sqlSelect("table", dialect = SqlDialect.MY_SQL) {
            where { column("col", WhereOps.IN, value) }
        }
            .expectSqlAndParams(
                "select * from `table` where `table`.`col` in(?)",
                listOf(value)
            )
    }
    "select from table where col is null" {
        sqlSelect("table", dialect = SqlDialect.MY_SQL) {
            where { columnIsNull("col") }
        }
            .expectSqlAndParams("select * from `table` where `table`.`col` is null")
    }
    "select from table where col is not null" {
        sqlSelect("table", dialect = SqlDialect.MY_SQL) {
            where { columnNotNull("col") }
        }
            .expectSqlAndParams("select * from `table` where `table`.`col` is not null")
    }
    "select from table with native sql condition" {
        val name = "bob"
        sqlSelect("table", dialect = SqlDialect.MS_SQL) {
            where {
                native("col") {
                    "json_value($c, '$.name') = ${v(name)}"
                }
            }
        }
            .expectSqlAndParams("select * from [table] where json_value([table].[col], '\$.name') = ?", listOf(name))
    }
})