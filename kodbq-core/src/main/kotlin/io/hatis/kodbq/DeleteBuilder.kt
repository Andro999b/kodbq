package io.hatis.kodbq

class DeleteBuilder(
    val tableName: String,
    val where: WherePart,
    override val dialect: SqlDialect = SqlDialect.PG
): AbstractSqlBuilder() {
    override fun buildSqlAndParams(): Pair<String, List<Any?>> {
        val escape = dialect.escape
        val params = mutableListOf<Any?>()

        val wherePart = WhereBuilder(buildOptions, buildOptions.paramPlaceholder, params).build(where)
        val sql = "delete from ${escape(tableName)} where $wherePart"

        return sql to params
    }
}
