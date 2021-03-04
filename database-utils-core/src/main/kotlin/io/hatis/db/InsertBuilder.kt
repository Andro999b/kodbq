package io.hatis.db

class InsertBuilder(
    val tableName: String,
    val values: List<Map<Column, Any?>>,
    val generatedKeys: Set<String> = emptySet(),
    val mode: SqlMode = SqlMode.PG
) {
    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<List<Any?>>> {
        val firstRow = values.first()
        val columns = firstRow.keys
        var sql = "insert into ${mode.escape(tableName)}(${columns.joinToString(",") { it.toString() }}) " +
                "values(${columns.mapIndexed { i, _ -> paramPlaceholder(i + 1) }.joinToString(",")})"

        if(generatedKeys.isNotEmpty()) {
            if(mode == SqlMode.PG) {
                sql += " returning ${generatedKeys.joinToString(",", transform = mode.escape)}"
            }
        }

        return sql to values.map { it.values.toList() }
    }
}