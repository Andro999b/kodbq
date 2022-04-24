package io.hatis.kodbq

class InsertBuilder(
    val tableName: String,
    val dialect: SqlDialect = SqlDialect.PG,
    val values: List<Map<Column, Any?>>,
    val generatedKeys: Set<String> = emptySet()
): SqlBuilder {
    private fun buildColumnsAndValue(
        outParams: MutableList<Any?>,
        paramPlaceholder: (Int) -> String,
        columns: Set<Map.Entry<Column, Any?>>
    ): List<Pair<Column, String?>> {
        return columns
            .map { (key, value) ->
                if (value is NativeSqlColumn) {
                    key to value.generate(
                        NativeSqlColumn.Usage.INSERT,
                        outParams = outParams,
                        paramPlaceholder = paramPlaceholder
                    )
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
    ) = columns.forEach { (_, value) ->
            if (value is NativeSqlColumn) {
                value.generate(
                    NativeSqlColumn.Usage.INSERT,
                    outParams = outParams,
                    paramPlaceholder = paramPlaceholder
                )
            } else {
                outParams.add(value)
            }
        }

    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<List<Any?>>> {
        val valuesItr = values.listIterator()
        val firstRow = valuesItr.next()
        val firstParams = mutableListOf<Any?>()
        val keyValues = buildColumnsAndValue(firstParams, paramPlaceholder, firstRow.entries)
        var sql = "insert into ${dialect.escape(tableName)}(${keyValues.joinToString(",") { it.first.toString() }}) " +
                "values(${keyValues.joinToString(",") { it.second.toString() }})"

        val params = mutableListOf<List<Any?>>()
        params += firstParams

        if (generatedKeys.isNotEmpty()) {
            if (dialect == SqlDialect.PG) {
                sql += " returning ${generatedKeys.joinToString(",", transform = dialect.escape)}"
            }
        }

        valuesItr.forEach {
            val rowParams = mutableListOf<Any?>()
            buildParams(rowParams, paramPlaceholder, it.entries)
            params += rowParams
        }

        return sql to params
    }
}