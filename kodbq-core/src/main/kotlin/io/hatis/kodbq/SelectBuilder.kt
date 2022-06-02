package io.hatis.kodbq

class SelectBuilder(
    private val selects: Collection<Select>,
    override val dialect: SqlDialect
) : AbstractSqlBuilder() {
    data class Sort(val column: Named, val asc: Boolean = true)
    data class Limit(val offset: Int = 0, val count: Int = 0)

    enum class JoinMode(val sql: String) { INNER(""), LEFT("left"), RIGHT("right"), FULL("full"), }
    data class Join(
        val joinTableColumn: Column,
        val onColumn: Column,
        val joinMode: JoinMode
    )

    data class Returns(
        val columns: Set<ReturnColumn> = emptySet(),
        val functions: Map<String, Function> = emptyMap()
    )

    data class Select(
        val table: Table,
        val joins: Collection<Join>,
        val where: WherePart?,
        val having: WherePart?,
        val sort: Collection<Sort> = emptySet(),
        val limit: Limit? = null,
        val distinct: Boolean = false,
        val returns: Returns,
        val groupByColumns: Set<Column> = emptySet(),
        val unionAll: Boolean = false
    )

    override fun buildSqlAndParams(): Pair<String, List<Any?>> {
        val params = mutableListOf<Any?>()
        val sql = buildString {
            selects.forEachIndexed { index, select ->
                if (index > 0) {
                    append("\n")
                    append("union")
                    if (select.unionAll) append(" all")
                    append("\n")
                }
                buildSelect(select, params)
            }
        }

        return sql to params
    }

    private fun StringBuilder.buildSelect(select: Select, params: MutableList<Any?>) {
        val escape = dialect.escape
        with(select) {
            if (distinct) append("select distinct ") else append("select ")
            val allColumnNames = (
                    returns.columns.map { columnToReturnField(it) } +
                            groupByColumns.map { it.toString() } +
                            getFunctions(returns, params)
                    ).toSet()

            if (allColumnNames.isEmpty()) {
                append("*")
            } else {
                appendWithDelimiter(allColumnNames)
            }

            append(" from ")
            append(escape(table.name))

            buildJoins(joins)
            buildFilter(where, params)
            buildGroupBy(groupByColumns, having, params)
            buildSort(sort)

            if (dialect == SqlDialect.MS_SQL) {
                buildMSSQLLimit(limit, sort)
            } else {
                buildLimit(limit)
            }
        }
    }

    private fun StringBuilder.buildJoins(joins: Collection<Join>) {
        joins.forEach { (joinTableColumn, onColumn, joinMode) ->
            if (joinMode.sql.isNotEmpty()) {
                append(" ")
                append(joinMode.sql)
            }
            append(" join ")
            append(joinTableColumn.escapeTable)
            append(" on ")
            append(joinTableColumn)
            append("=")
            append(onColumn)
        }
    }

    private fun StringBuilder.buildFilter(where: WherePart?, params: MutableList<Any?>) {
        where?.let {
            val whereString = WhereBuilder(buildOptions, buildOptions.paramPlaceholder, params).build(it)
            if (whereString.isNotEmpty()) {
                append(" where ")
                append(whereString)
            }
        }
    }

    private fun StringBuilder.buildGroupBy(groupByColumns: Set<Column>, having: WherePart?, params: MutableList<Any?>) {
        if (groupByColumns.isNotEmpty()) {
            append(" group by ")
            appendWithDelimiter(groupByColumns.map { it.toString() })
            having?.let {
                val whereString = WhereBuilder(buildOptions, buildOptions.paramPlaceholder, params).build(it)
                if (whereString.isNotEmpty()) {
                    append(" having ")
                    append(whereString)
                }
            }
        }
    }

    private fun StringBuilder.buildSort(sort: Collection<Sort>) {
        if (sort.isNotEmpty()) {
            append(" order by ")
            appendWithDelimiter(sort.map { "${it.column}${if (it.asc) "" else " desc"}" })
        }
    }

    private fun StringBuilder.buildLimit(limit: Limit?) {
        limit?.let {
            if (it.count > 0) {
                append(" limit ")
                append(it.count)
            }
            if (it.offset > 0) {
                append(" offset ")
                append(it.offset)
            }
        }
    }

    private fun StringBuilder.buildMSSQLLimit(limit: Limit?, sort: Collection<Sort>) {
        limit?.let {
            if (sort.isEmpty()) {
                throw KodbqException("Cant use limit/offset without sort in MS_SQL")
            }
            if (it.count > 0 || it.offset > 0) {
                append(" offset ")
                append(it.offset)
                append(" rows")
                if (it.count > 0) {
                    append(" fetch next ")
                    append(it.count)
                    append(" rows only")
                }
            }
        }
    }

    private fun StringBuilder.appendWithDelimiter(strings: Collection<String>) {
        strings.forEach {
            append(it)
            append(",")
        }
        delete(length - 1, length)
    }

    private fun columnToReturnField(c: ReturnColumn): String =
        if (c.alias != null)
            "$c as ${c.alias}"
        else
            c.toString()

    private fun getFunctions(returns: Returns, params: MutableList<Any?>): Collection<String> =
        returns.functions.entries.mapNotNull { (alias, f) ->
            when (f) {
                is NativeFunction -> {
                    val functionSql = f.nativeSqlColumn.generate(
                        outParams = params,
                        paramPlaceholder = buildOptions.paramPlaceholder
                    )
                    "$functionSql as $alias"
                }
                is SimpleFunction -> "$f as $alias"
                else -> null
            }
        }
}