package io.hatis.db

class UpdateBuilder(
    val tableName: String,
    val columns: Map<Column, Any?>,
    val where: WherePart?,
    val mode: SqlMode = SqlMode.PG
) {
    private fun buildParams(outParams: MutableList<Any?>, paramPlaceholder: (Int) -> String) =
        columns.entries
            .mapNotNull { (key, value) ->
                if (value is SqlGenerator.GeneratedPart) {
                    val actions = value.actions
                    val generator = SqlGenerator(
                        usage = SqlGenerator.Usage.update,
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


    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val params = mutableListOf<Any?>()
        val whereParams = mutableListOf<Any?>()

        var sql = "update ${mode.escape(tableName)} set " + buildParams(params, paramPlaceholder)

        where?.let {
            sql += " where ${buildWhere(it, whereParams, mode.escape, paramPlaceholder, params.size)}"
        }

        return sql to (params + whereParams)
    }
}