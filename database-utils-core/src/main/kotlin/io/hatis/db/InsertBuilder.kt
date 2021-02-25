package io.hatis.db

class InsertBuilder(
    val tableName: String,
    val values: List<Map<Column, Any?>>,
    val generatedKeys: Set<String> = emptySet(),
    val mode: SqlMode = SqlMode.PG
) {
    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<List<Any?>>> {
        val firstRow = values.first()
        val columnsNames = firstRow.keys
        var sql = "insert into ${mode.escape(tableName)}(${columnsNames.joinToString(",") { it.toString() }}) " +
                "values(${(1..columnsNames.size).joinToString(",", transform = paramPlaceholder)})"

        if(generatedKeys.isNotEmpty()) {
            sql += " returning ${generatedKeys.joinToString(",", transform = mode.escape)}"
        }

        return sql to values.map { it.values.toList() }
    }
}