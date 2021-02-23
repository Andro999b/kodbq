package io.hatis.db.quarkus
import io.hatis.db.DeleteBuilder
import io.hatis.utils.db.io.hatis.*
import java.lang.StringBuilder

fun DeleteBuilder.buildSqlAndParams(): Pair<String, List<Any>> {
    val params = mutableListOf<Any>()
    val sql = "delete from \"$tableName\" where ${buildWhere(where, params)}"

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

fun UpdateBuilder.buildSqlAndParams(): Pair<String, List<Any?>> {
    val columnsNames = this.columns.keys
    val params = this.columns.values
    val whereParams = mutableListOf<Any>()

    var sql = "update \"$tableName\" set ${columnsNames.mapIndexed { i, c -> "\"$c\"=$${i + 1}" }}"

    where?.let {
        sql += " where ${buildWhere(it, whereParams, params.size)}"
    }

    return sql to (params + whereParams)
}

fun SelectBuilder.buildSqlAndParams(): Pair<String, List<Any>> {
    val params = mutableListOf<Any>()
    val sql = StringBuilder("select ")

    if(columns.isNotEmpty()) {
        sql.append(columns.joinToString(","){ "\"$it\"" })
    } else {
        sql.append("*")
    }

    sql.append(" from \"$tableName\"")

    where?.let {
        sql.append(" where ").append(buildWhere(it, params))
    }

    sort?.let {
        sql.append(" sort on \"${it.column}\" ${if(it.asc) "asc" else "desc" }")
    }

    limit?.let {
        sql.append(" limit ${it.offset}${if (it.count > 0) ", ${it.count}" else ""}")
    }

    return sql.toString() to params
}

internal fun buildWhere(wherePart: WherePart, outParams: MutableList<Any>, paramsIndexOffset: Int = 0): String =
    when (wherePart) {
        is Join ->
            if(wherePart.parts.size == 1) {
                buildWhere(wherePart.parts.first(), outParams, paramsIndexOffset)
            } else {
                "(" + wherePart.parts.joinToString(wherePart.separator) {
                    buildWhere(it, outParams, paramsIndexOffset)
                } + ")"
            }
        is Column -> {
            outParams.add(wherePart.params)
            when (wherePart.op) {
                WhereOps.like -> "\"${wherePart.columnName}\" like %$${outParams.size + paramsIndexOffset}%"
                WhereOps.`in` -> "\"${wherePart.columnName}\" in ($${outParams.size + paramsIndexOffset})"
                else -> "\"${wherePart.columnName}\" ${wherePart.op.op} $${outParams.size + paramsIndexOffset}"
            }
        }
        is ColumnIsNotNull -> "\"${wherePart.columnName}\" is not null"
        is ColumnIsNull -> "\"${wherePart.columnName}\" is null"
        else -> throw IllegalArgumentException("Unknown where part $wherePart")
    }