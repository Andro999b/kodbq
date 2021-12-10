package io.hatis.db

class InsertBuilder(
    val tableName: String,
    val mode: SqlMode = SqlMode.PG,
    val values: List<Map<Column, Any?>>,
    val generatedKeys: Set<String> = emptySet()
): SqlBuilder {
    private fun buildColumnsAndValue(
        outParams: MutableList<Any?>,
        paramPlaceholder: (Int) -> String,
        columns: Set<Map.Entry<Column, Any?>>
    ): List<Pair<Column, String?>> {
        return columns
            .mapNotNull { (key, value) ->
                if (value is DSLColumnSqlGenerator.CustomSqlPart) {
                    val actions = value.actions
                    val generator = DSLColumnSqlGenerator(
                        usage = DSLColumnSqlGenerator.Usage.insert,
                        outParams = outParams,
                        paramPlaceholder = paramPlaceholder,
                        column = key
                    )
                    generator.actions()
                    generator.generatedSql?.let { key to it }
                } else {
                    outParams.add(value)
                    key to paramPlaceholder(outParams.size)
                }
            }
    }

    private fun buildParams(
        outParams: MutableList<Any?>,
        paramPlaceholder: (Int) -> String,
        columns: Set<Map.Entry<Column, Any?>>
    ) = columns.forEach { (key, value) ->
            if (value is DSLColumnSqlGenerator.CustomSqlPart) {
                val actions = value.actions
                val generator = DSLColumnSqlGenerator(
                    usage = DSLColumnSqlGenerator.Usage.insert,
                    outParams = outParams,
                    paramPlaceholder = paramPlaceholder,
                    column = key
                )
                generator.actions()
            } else {
                outParams.add(value)
            }
        }

    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<List<Any?>>> {
        val firstRow = values.first()
        val keyValues = buildColumnsAndValue(mutableListOf(), paramPlaceholder, firstRow.entries)
        var sql = "insert into ${mode.escape(tableName)}(${keyValues.joinToString(",") { it.first.toString() }}) " +
                "values(${keyValues.joinToString(",") { it.second.toString() }})"

        if (generatedKeys.isNotEmpty()) {
            if (mode == SqlMode.PG) {
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