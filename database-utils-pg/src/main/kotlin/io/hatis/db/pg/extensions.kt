package io.hatis.db.pg
import io.hatis.db.*

fun DeleteBuilder.buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any>> {
    val params = mutableListOf<Any>()
    val sql = "delete from \"$tableName\" where ${buildWhere(where, params, paramPlaceholder)}"

    return sql to params
}

fun InsertBuilder.buildSqlAndParams(): Pair<String, List<Any?>> {
    val columnsNames = this.columns.keys
    val params = this.columns.values.toList()
    var sql = "insert into \"$tableName\"(${columnsNames.joinToString(",") { "\"$it\"" }}) " +
            "values(${(1..params.size).joinToString(",") { "$$it" }})"

    if(generatedKeys.isNotEmpty()) {
        sql += " returning ${generatedKeys.joinToString(",") { "\"$it\"" }}"
    }

    return sql to params
}

fun UpdateBuilder.buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
    val columnsNames = this.columns.keys
    val params = this.columns.values
    val whereParams = mutableListOf<Any>()

    var sql = "update \"$tableName\" set ${columnsNames.mapIndexed { i, c -> "\"$c\"=$${i + 1}" }.joinToString(",")}"

    where?.let {
        sql += " where ${buildWhere(it, whereParams, paramPlaceholder, params.size)}"
    }

    return sql to (params + whereParams)
}

fun SelectBuilder.buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any>> {
    val params = mutableListOf<Any>()
    val sql = if(distinct) StringBuilder("distinct select ") else StringBuilder("select ")


    if(columns.isNotEmpty()) {
        sql.append(columns.joinToString(","){ "\"$it\"" })
        getAggregationPart()?.let { sql.append(",").append(it) }
    } else {
        getAggregationPart()
            ?.let { sql.append(it) }
            ?: sql.append("*")
    }

    sql.append(" from \"$tableName\"")

    where?.let {
        sql.append(" where ").append(buildWhere(it, params, paramPlaceholder))
    }

    aggregation?.let { (groupBy, _) ->
        if(groupBy.isNotEmpty())
            sql.append(" group by ").append(groupBy.joinToString(","){ "\"$it\"" })
    }

    sort?.let {
        sql.append(" sort on \"${it.column}\" ${if(it.asc) "asc" else "desc" }")
    }

    limit?.let {
        sql.append(" limit ${it.offset}${if (it.count > 0) ", ${it.count}" else ""}")
    }

    return sql.toString() to params
}

private fun SelectBuilder.getAggregationPart() =
    aggregation?.let {
        it.functions.entries.map { (alias, f) ->
            when (f) {
                is SelectBuilder.ColumnFunction ->
                    "${f.function}(${if (f.column == "*") "*" else "\"" + f.column + "\""}) as \"$alias\""
                is SelectBuilder.DBFunction ->
                    "${f.function} as \"$alias\""
                else -> null
            }
        }
            .filterNotNull()
            .joinToString(",")
    }

internal fun buildWhere(
    wherePart: WherePart,
    outParams: MutableList<Any>,
    paramPlaceholder: (Int) -> String,
    paramsIndexOffset: Int = 0
): String =
    when (wherePart) {
        is Join ->
            if(wherePart.parts.size == 1) {
                buildWhere(wherePart.parts.first(), outParams, paramPlaceholder, paramsIndexOffset)
            } else {
                "(" + wherePart.parts.joinToString(wherePart.separator) {
                    buildWhere(it, outParams, paramPlaceholder, paramsIndexOffset)
                } + ")"
            }
        is Column -> {
            outParams.add(wherePart.params)
            when (wherePart.op) {
                WhereOps.like -> "\"${wherePart.columnName}\" like %${paramPlaceholder(outParams.size + paramsIndexOffset)}%"
                WhereOps.`in` -> "\"${wherePart.columnName}\" in (${paramPlaceholder(outParams.size + paramsIndexOffset)})"
                else -> "\"${wherePart.columnName}\" ${wherePart.op.op} ${paramPlaceholder(outParams.size + paramsIndexOffset)}"
            }
        }
        is ColumnIsNotNull -> "\"${wherePart.columnName}\" is not null"
        is ColumnIsNull -> "\"${wherePart.columnName}\" is null"
        else -> throw IllegalArgumentException("Unknown where part $wherePart")
    }