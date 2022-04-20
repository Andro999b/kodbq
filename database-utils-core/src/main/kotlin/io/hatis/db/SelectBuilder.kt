package io.hatis.db

class SelectBuilder(
    val tableName: String,
    val columns: Collection<Column>,
    val joins: Collection<Join>,
    val where: WherePart?,
    val sort: Collection<Sort> = emptySet(),
    val limit: Limit? = null,
    val distinct: Boolean = false,
    val aggregation: Aggregation? = null,
    val mode: SqlMode = SqlMode.PG
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

    interface AggregationFunction
    data class ColumnFunction(val function: String, val column: Column? = null) : AggregationFunction
    data class DBFunction(val function: String) : AggregationFunction
    data class Aggregation(val groupBy: Collection<Column>, val functions: Map<String, AggregationFunction>)

    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val escape = mode.escape
        val params = mutableListOf<Any?>()
        val sql = if (distinct) StringBuilder("select distinct ") else StringBuilder("select ")

        val allColumns = columns.map { columnToReturnField(it) } + getAggregationColumns()

        if (allColumns.isEmpty()) {
            sql.append("*")
        } else {
            sql.append(allColumns.joinToString(","))
        }

        sql.append(" from ${escape(tableName)}")

        joins.forEach { (joinTableColumn, onColumn, joinMode, alias) ->
            if (joinMode.sql.isNotEmpty()) sql.append(" ").append(joinMode.sql)
            sql.append(" join ${joinTableColumn.escapeTable()}${alias?.let { " as $it" } ?: ""} on $joinTableColumn = $onColumn")
        }

        where?.let {
            val whereString = buildWhere(it, params, escape, paramPlaceholder)
            if (whereString?.isNotEmpty() == true)
                sql.append(" where ").append(whereString)
        }

        aggregation?.let { (groupBy, _) ->
            if (groupBy.isNotEmpty())
                sql.append(" group by ").append(groupBy.joinToString(","))
        }

        if (sort.isNotEmpty()) {
            sql.append(" order by ")
                .append(sort.joinToString(",") { "${it.column} ${if (it.asc) "asc" else "desc"}" })
        }

        limit?.let {
            sql.append(" offset ").append(it.offset)
            if (it.count > 0) {
                sql.append(" limit ").append(it.count)
            }
        }

        return sql.toString() to params
    }

    private fun columnToReturnField(c: Column): String =
        if (c.alias != null) "$c as ${c.alias}" else c.toString()

    private fun getAggregationColumns(): Collection<String> =
        aggregation?.let {
            it.functions.entries.mapNotNull { (alias, f) ->
                when (f) {
                    is ColumnFunction ->
                        "${f.function}(${f.column ?: "*"}) as ${mode.escape(alias)}"
                    is DBFunction ->
                        "${f.function} as ${mode.escape(alias)}"
                    else -> null
                }
            } + it.groupBy.map { c -> columnToReturnField(c) }
        } ?: emptySet()
}