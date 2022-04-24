package io.hatis.kodbq

class SelectBuilder(
    val tableName: String,
    val joins: Collection<Join>,
    val where: WherePart?,
    val sort: Collection<Sort> = emptySet(),
    val limit: Limit? = null,
    val distinct: Boolean = false,
    val returns: Returns,
    val groupByColumns: Set<Column> = emptySet(),
    val dialect: SqlDialect
) : SqlBuilder {
    data class Sort(val column: Column, val asc: Boolean = true)
    data class Limit(val offset: Int = 0, val count: Int = 0)

    enum class JoinMode(val sql: String) { INNER(""), LEFT("left"), RIGHT("right"), FULL("full"), }
    data class Join(
        val joinTableColumn: Column,
        val onColumn: Column,
        val joinMode: JoinMode,
        var alias: String? = null
    )

    data class Returns(
        val columns: Set<Column> = emptySet(),
        val functions: Map<String, Function> = emptyMap()
    )

    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val escape = dialect.escape
        val params = mutableListOf<Any?>()
        val sql = buildString {
            if (distinct) append("select distinct") else append("select ")
            val allColumns = returns.columns + groupByColumns
            val allColumnNames = allColumns.map { columnToReturnField(it) } + getFunctions(params, paramPlaceholder)

            if (allColumnNames.isEmpty()) {
                append("*")
            } else {
                appendWithDelimiter(allColumnNames)
            }

            append(" from ")
            append(escape(tableName))

            joins.forEach { (joinTableColumn, onColumn, joinMode, alias) ->
                if (joinMode.sql.isNotEmpty()) {
                    append(" ")
                    append(joinMode.sql)
                }
                append(" join ")
                append(joinTableColumn.escapeTable())
                alias?.let {
                    append(" as ")
                    append(it)
                }
                append(" on ")
                append(joinTableColumn)
                append(" = ")
                append(onColumn)
            }

            where?.let {
                val whereString = buildWhere(it, params, escape, paramPlaceholder)
                if (whereString.isNotEmpty()) {
                    append(" where ")
                    append(whereString)
                }
            }

            if (groupByColumns.isNotEmpty()) {
                append(" group by ")
                appendWithDelimiter(groupByColumns.map { it.toString() })
            }

            if (sort.isNotEmpty()) {
                append(" order by ")
                appendWithDelimiter(sort.map { "${it.column} ${if (it.asc) "asc" else "desc"}" })
            }

            limit?.let {
                append(" offset ")
                append(it.offset)
                if (it.count > 0) {
                    append(" limit ")
                    append(it.count)
                }
            }
        }

        return sql to params
    }

    private fun columnToReturnField(c: Column): String =
        if (c.alias != null) "$c as ${c.alias}" else c.toString()

    private fun getFunctions(params: MutableList<Any?>, paramPlaceholder: (Int) -> String): Collection<String> =
        returns.functions.entries.mapNotNull { (alias, f) ->
            when (f) {
                is NativeFunction -> {
                    val functionSql = f.nativeSqlColumn.generate(
                        outParams = params,
                        paramPlaceholder = paramPlaceholder
                    )
                    "$functionSql as $alias"
                }
                is SimpleFunction ->
                    "${f.function}(${f.column}) as $alias"
                else -> null
            }
        }

    private fun StringBuilder.appendWithDelimiter(strings: Collection<String>) {
        strings.forEach {
            append(it)
            append(",")
        }
        delete(length - 1, length)
    }
}