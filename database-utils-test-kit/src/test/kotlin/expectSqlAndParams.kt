import io.hatis.db.SqlBuilder
import io.kotest.matchers.shouldBe

fun expectSqlAndParams(sql: String, params: List<Any?> = emptyList()): (builder: SqlBuilder) -> Unit = {
    val (actualSql, actualParams) = it.buildSqlAndParams { "?" }

    actualSql shouldBe sql
    actualParams shouldBe params
}