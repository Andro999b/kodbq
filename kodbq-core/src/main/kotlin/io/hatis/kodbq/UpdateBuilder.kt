package io.hatis.kodbq

class UpdateBuilder(
    val tableName: String,
    val columns: Map<Column, Any?>,
    val where: WherePart?,
    override val dialect: SqlDialect
): AbstractSqlBuilder() {
    private fun buildParams(outParams: MutableList<Any?>) =
        columns.entries.mapNotNull { (key, value) ->
            if (value is NativeSqlColumn) {
                val sql = value.generate(
                    NativeSqlColumn.Usage.UPDATE,
                    outParams = outParams,
                    paramPlaceholder = buildOptions.paramPlaceholder
                )
                "$key=$sql"
            } else {
                outParams.add(value)
                "$key=${buildOptions.paramPlaceholder(outParams.size)}"
            }
        }
            .joinToString(",")


    override fun buildSqlAndParams(): Pair<String, List<Any?>> {
        val params = mutableListOf<Any?>()
        val whereParams = mutableListOf<Any?>()

        var sql = "update ${dialect.escape(tableName)} set " + buildParams(params)

        where?.let {
            val wherePart = WhereBuilder(buildOptions, buildOptions.paramPlaceholder, whereParams, params.size).build(it)
            sql += " where $wherePart"
        }

        return sql to (params + whereParams)
    }
}