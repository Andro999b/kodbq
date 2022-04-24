package io.hatis.kodbq

class DeleteBuilder(
    val tableName: String,
    val where: WherePart,
    val mode: SqlDialect = SqlDialect.PG
): SqlBuilder {
    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val escape = mode.escape
        val params = mutableListOf<Any?>()
        val sql = "delete from ${escape(tableName)} where ${buildWhere(where, params, escape, paramPlaceholder)}"

        return sql to params
    }
}
