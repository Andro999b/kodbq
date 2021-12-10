package io.hatis.db

class UpdateBuilder(
    val tableName: String,
    val columns: Map<Column, Any?>,
    val where: WherePart?,
    val mode: SqlMode = SqlMode.PG
): SqlBuilder {
    private fun buildParams(outParams: MutableList<Any?>, paramPlaceholder: (Int) -> String) =
        columns.entries
            .mapNotNull { (key, value) ->
                if (value is DSLColumnSqlGenerator.CustomSqlPart) {
                    val actions = value.actions
                    val generator = DSLColumnSqlGenerator(
                        usage = DSLColumnSqlGenerator.Usage.update,
                        outParams = outParams,
                        paramPlaceholder = paramPlaceholder,
                        column = key
                    )
                    generator.actions()
                    generator.generatedSql
                } else {
                    outParams.add(value)
                    "$key=${paramPlaceholder(outParams.size)}"
                }
            }
            .joinToString(",")


    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val params = mutableListOf<Any?>()
        val whereParams = mutableListOf<Any?>()

        var sql = "update ${mode.escape(tableName)} set " + buildParams(params, paramPlaceholder)

        where?.let {
            sql += " where ${buildWhere(it, whereParams, mode.escape, paramPlaceholder, params.size)}"
        }

        return sql to (params + whereParams)
    }
}