package io.hatis.kodbq

class UpdateBuilder(
    val tableName: String,
    val columns: Map<Column, Any?>,
    val where: WherePart?,
    val dialect: SqlDialect
): SqlBuilder {
    private fun buildParams(outParams: MutableList<Any?>, paramPlaceholder: (Int) -> String) =
        columns.entries.mapNotNull { (key, value) ->
            if (value is NativeSqlColumn) {
                value.generate(
                    NativeSqlColumn.Usage.UPDATE,
                    outParams = outParams,
                    paramPlaceholder = paramPlaceholder
                )
            } else {
                outParams.add(value)
                "$key=${paramPlaceholder(outParams.size)}"
            }
        }
            .joinToString(",")


    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val params = mutableListOf<Any?>()
        val whereParams = mutableListOf<Any?>()

        var sql = "update ${dialect.escape(tableName)} set " + buildParams(params, paramPlaceholder)

        where?.let {
            sql += " where ${buildWhere(it, whereParams, paramPlaceholder, params.size)}"
        }

        return sql to (params + whereParams)
    }
}