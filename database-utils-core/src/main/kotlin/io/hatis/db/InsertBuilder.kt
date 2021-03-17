package io.hatis.db

class InsertBuilder(
    val tableName: String,
    val values: List<Map<Column, Any?>>,
    val generatedKeys: Set<String> = emptySet(),
    val mode: SqlMode = SqlMode.PG
) {
    private fun buildParamsAndSql(outParams: MutableList<Any?>, paramPlaceholder: (Int) -> String, columns: Set<Map.Entry<Column, Any?>>) =
        columns.joinToString(",") { (key, value) ->
            if(value is SqlGenerator.GeneratedPart) {
                val actions = value.actions
                val generator = SqlGenerator(
                    outParams = outParams,
                    paramPlaceholder = paramPlaceholder,
                    column = key
                )
                generator.actions()
                generator.generatedSql
            } else {
                outParams.add(value)
                paramPlaceholder(outParams.size)
            }
        }

    private fun buildParams(outParams: MutableList<Any?>, paramPlaceholder: (Int) -> String, columns: Set<Map.Entry<Column, Any?>>) =
        columns.forEach { (key, value) ->
            if(value is SqlGenerator.GeneratedPart) {
                val actions = value.actions
                val generator = SqlGenerator(
                    outParams = outParams,
                    paramPlaceholder = paramPlaceholder,
                    column = key
                )
                generator.actions()
            } else {
                outParams.add(value)
            }
        }

    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<List<Any?>>> {
        val firstRow = values.first()
        val columns = firstRow.entries
        var sql = "insert into ${mode.escape(tableName)}(${columns.joinToString(",") { it.key.toString() }}) " +
                "values(${buildParamsAndSql(mutableListOf(), paramPlaceholder, columns) })"

        if(generatedKeys.isNotEmpty()) {
            if(mode == SqlMode.PG) {
                sql += " returning ${generatedKeys.joinToString(",", transform = mode.escape)}"
            }
        }

        return sql to values.map {
            val outParams = mutableListOf<Any?>()
            buildParams(outParams, paramPlaceholder, it.entries)
            outParams
        }
    }
}