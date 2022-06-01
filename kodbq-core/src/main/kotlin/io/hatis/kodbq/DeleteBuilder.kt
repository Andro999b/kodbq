package io.hatis.kodbq

class DeleteBuilder(
    val table: Table,
    val where: WherePart?,
    override val dialect: SqlDialect = SqlDialect.PG
): AbstractSqlBuilder() {
    override fun buildSqlAndParams(): Pair<String, List<Any?>> {
        val escape = dialect.escape
        val params = mutableListOf<Any?>()

        val sql = if(where != null) {
            val wherePart = WhereBuilder(buildOptions, buildOptions.paramPlaceholder, params).build(where)
            "delete from ${escape(table.name)} where $wherePart"
        } else {
            "delete from ${escape(table.name)}"
        }

        return sql to params
    }
}
