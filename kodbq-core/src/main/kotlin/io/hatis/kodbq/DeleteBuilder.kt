package io.hatis.kodbq

class DeleteBuilder(
    val tableName: String,
    val where: WherePart?,
    override val dialect: SqlDialect = SqlDialect.PG
): AbstractSqlBuilder() {
    override fun buildSqlAndParams(): Pair<String, List<Any?>> {
        val escape = dialect.escape
        val params = mutableListOf<Any?>()

        val sql = if(where != null) {
            val wherePart = WhereBuilder(buildOptions, buildOptions.paramPlaceholder, params).build(where)
            "delete from ${escape(tableName)} where $wherePart"
        } else {
            "delete from ${escape(tableName)}"
        }

        return sql to params
    }
}
