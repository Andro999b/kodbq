package io.hatis.kodbq

class InsertBuilder(
    val tableName: String,
    override val dialect: SqlDialect = SqlDialect.PG,
    val values: List<Map<Column, Any?>>,
    val generatedKeys: Set<String> = emptySet()
): AbstractSqlBuilder() {
    private fun buildColumnsAndValue(
        outParams: MutableList<Any?>,
        columns: Set<Map.Entry<Column, Any?>>
    ): List<Pair<Column, String?>> {
        return columns
            .map { (key, value) ->
                if (value is NativeSqlColumn) {
                    key to value.generate(
                        NativeSqlColumn.Usage.INSERT,
                        outParams = outParams,
                        paramPlaceholder = buildOptions.paramPlaceholder
                    )
                } else {
                    outParams.add(value)
                    key to buildOptions.paramPlaceholder(outParams.size)
                }
            }
            .filter { it.second != null }
    }

    private fun buildParams(
        outParams: MutableList<Any?>,
        columns: Set<Map.Entry<Column, Any?>>
    ) = columns.forEach { (_, value) ->
            if (value is NativeSqlColumn) {
                value.generate(
                    NativeSqlColumn.Usage.INSERT,
                    outParams = outParams,
                    paramPlaceholder = buildOptions.paramPlaceholder
                )
            } else {
                outParams.add(value)
            }
        }

    override fun buildSqlAndParams(): Pair<String, List<List<Any?>>> {
        val valuesItr = values.listIterator()
        val firstRow = valuesItr.next()
        val firstParams = mutableListOf<Any?>()
        val keyValues = buildColumnsAndValue(firstParams, firstRow.entries)
        var sql = "insert into ${dialect.escape(tableName)}(${keyValues.joinToString(",") { it.first.toString() }})"

        if (buildOptions.generatedKeysSql && generatedKeys.isNotEmpty()) {
            if (dialect == SqlDialect.MS_SQL) {
                sql += " output ${generatedKeys.joinToString(",") { "inserted.${dialect.escape(it)}" }}"
            }
        }

        sql += " values(${keyValues.joinToString(",") { it.second.toString() }})"

        val params = mutableListOf<List<Any?>>()
        params += firstParams

        if (buildOptions.generatedKeysSql && generatedKeys.isNotEmpty()) {
            if (dialect == SqlDialect.PG) {
                sql += " returning ${generatedKeys.joinToString(",", transform = dialect.escape)}"
            }
        }

        valuesItr.forEach {
            val rowParams = mutableListOf<Any?>()
            buildParams(rowParams, it.entries)
            params += rowParams
        }

        return sql to params
    }
}