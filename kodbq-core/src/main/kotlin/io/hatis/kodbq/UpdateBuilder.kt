package io.hatis.kodbq

class UpdateBuilder(
    val tableName: String,
    val columns: Map<Column, Any?>,
    val where: WherePart?,
    val dialect: SqlDialect
): AbstractSqlBuilder() {
    private fun buildParams(outParams: MutableList<Any?>, paramPlaceholder: (Int) -> String) =
        columns.entries.mapNotNull { (key, value) ->
            if (value is NativeSqlColumn) {
                val sql = value.generate(
                    NativeSqlColumn.Usage.UPDATE,
                    outParams = outParams,
                    paramPlaceholder = paramPlaceholder
                )
                "$key=$sql"
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
            val wherePart = WhereBuilder(buildOptions, paramPlaceholder, whereParams, params.size).build(it)
            sql += " where $wherePart"
        }

        return sql to (params + whereParams)
    }
}