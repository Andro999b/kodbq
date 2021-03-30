package io.hatis.db

class SelectBuilder(
    val tableName: String,
    val columns: Collection<Column>,
    val joins: Collection<Join>,
    val where: WherePart?,
    val sort: Sort? = null,
    val limit: Limit? = null,
    val distinct: Boolean = false,
    val aggregation: Aggregation? = null,
    val mode: SqlMode = SqlMode.PG
) {
    data class Sort(val column: Column, val asc: Boolean = true)
    data class Limit(val offset: Int = 0, val count: Int = 0)

    enum class JoinMode(val sql: String) { INNER(""), LEFT("left"), RIGHT("right"), FULL("full"), }
    data class Join(val joinTableColumn: Column, val onColumn: Column, val joinMode: JoinMode)

    interface AggregationFunction
    data class ColumnFunction(val function: String, val column: Column? = null): AggregationFunction
    data class DBFunction(val function: String): AggregationFunction
    data class Aggregation(val groupBy: Collection<Column>, val functions: Map<String, AggregationFunction>)

    fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val escape = mode.escape
        val params = mutableListOf<Any?>()
        val sql = if(distinct) StringBuilder("select distinct ") else StringBuilder("select ")

        if(columns.isNotEmpty()) {
            sql.append(columns.joinToString(",") { columnToReturnField(it) })
            getAggregationPart()?.let { sql.append(",").append(it) }
        } else {
            getAggregationPart()
                ?.let { sql.append(it) }
                ?: sql.append("*")
        }

        sql.append(" from ${escape(tableName)}")

        joins.forEach { (joinTableColumn, onColumn, joinMode) ->
            if(joinMode.sql.isNotEmpty()) sql.append(" ").append(joinMode.sql)
            sql.append(" join ${joinTableColumn.escapeTable()} on ${joinTableColumn}=${onColumn}")
        }

        where?.let {
            val where = buildWhere(it, params, escape, paramPlaceholder)
            if(where.isNotEmpty())
                sql.append(" where ").append(where)
        }

        aggregation?.let { (groupBy, _) ->
            if(groupBy.isNotEmpty())
                sql.append(" group by ").append(groupBy.joinToString(","))
        }

        sort?.let {
            sql.append(" order by ${it.column} ${if(it.asc) "asc" else "desc" }")
        }

        limit?.let {
            sql.append(" offset ").append(it.offset)
            if(it.count > 0) {
                sql.append(" limit ").append(it.count)
            }
        }

        return sql.toString() to params
    }

    private fun columnToReturnField(c: Column): CharSequence =
        if (c.alias != null) "$c as ${c.alias}" else c.toString()

    private fun getAggregationPart() =
        aggregation?.let {
            it.functions.entries.mapNotNull { (alias, f) ->
                when (f) {
                    is ColumnFunction ->
                        "${f.function}(${f.column ?: "*"}) as ${mode.escape(alias)}"
                    is DBFunction ->
                        "${f.function} as ${mode.escape(alias)}"
                    else -> null
                }
            }
                .joinToString(",")
        }
}