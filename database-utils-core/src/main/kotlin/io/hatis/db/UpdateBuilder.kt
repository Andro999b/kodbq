package io.hatis.db

class UpdateBuilder(
    val tableName: String,
    val columns: Map<Column, Any?>,
    val where: WherePart?,
    val mode: SqlMode = SqlMode.PG
) {
    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val columns = this.columns.keys
        val params = this.columns.values
        val whereParams = mutableListOf<Any>()

        var sql = "update ${mode.escape(tableName)} set ${columns.mapIndexed { i, c -> "$c=${paramPlaceholder(i + 1)}" }.joinToString(",")}"

        where?.let {
            sql += " where ${buildWhere(it, whereParams, mode.escape, paramPlaceholder, params.size)}"
        }

        return sql to (params + whereParams)
    }
}