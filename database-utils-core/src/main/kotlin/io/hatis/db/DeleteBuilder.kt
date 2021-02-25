package io.hatis.db

class DeleteBuilder(
    val tableName: String,
    val where: WherePart,
    val mode: SqlMode = SqlMode.PG
) {
    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any>> {
        val escape = mode.escape
        val params = mutableListOf<Any>()
        val sql = "delete from ${escape(tableName)} where ${buildWhere(where, params, escape, paramPlaceholder)}"

        return sql to params
    }
}
