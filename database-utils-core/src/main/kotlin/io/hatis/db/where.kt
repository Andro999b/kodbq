package io.hatis.db

enum class WhereOps(val op: String) {
    eq("="),
    neq("!="),
    gt(">"),
    gte(">="),
    lt("<"),
    lte("<="),
    like("like"),
    `in`("in")
}

interface WherePart
abstract class WhereJoint(val separator: String) : WherePart {
    val parts: MutableList<WherePart> = mutableListOf()
}

data class WhereColumn(val column: Column, val op: WhereOps, val params: Any) : WherePart {
    val mode get() = column.mode
}

data class WhereGeneratedSql(val column: Column, val actions: SqlGenerator.() -> Unit) : WherePart
data class WhereColumnIsNull(val column: Column) : WherePart
data class WhereColumnIsNotNull(val column: Column) : WherePart
class Or : WhereJoint(" or ")
class And : WhereJoint(" and ")

internal fun buildWhere(
    wherePart: WherePart,
    outParams: MutableList<Any?>,
    escape: (String) -> String,
    paramPlaceholder: (Int) -> String,
    paramsIndexOffset: Int = 0
): String? =
    when (wherePart) {
        is WhereJoint ->
            when {
                wherePart.parts.size == 1 ->
                    buildWhere(wherePart.parts.first(), outParams, escape, paramPlaceholder, paramsIndexOffset)
                wherePart.parts.size > 1 -> {
                    val subSection = wherePart.parts
                        .mapNotNull { buildWhere(it, outParams, escape, paramPlaceholder, paramsIndexOffset) }
                        .joinToString(wherePart.separator)
                    "($subSection)"
                }
                else -> ""
            }
        is WhereColumn -> {
            when (wherePart.op) {
                WhereOps.`in` -> { // expand in params
                    val params = wherePart.params as Collection<*>
                    when (wherePart.mode) {
                        SqlMode.PG -> {
                            outParams.add(params.toTypedArray())
                            "${wherePart.column} = any(${paramPlaceholder(outParams.size + paramsIndexOffset)})"
                        }
                        else -> {
                            outParams.add(params)
                            "${wherePart.column} in (${paramPlaceholder(outParams.size + paramsIndexOffset)})"
                        }
                    }
                }
                else -> {
                    outParams.add(wherePart.params)
                    val column = wherePart.column
                    val placeholder = paramPlaceholder(outParams.size + paramsIndexOffset)
                    "$column ${wherePart.op.op} $placeholder"
                }
            }
        }
        is WhereColumnIsNotNull -> "${wherePart.column} is not null"
        is WhereColumnIsNull -> "${wherePart.column} is null"
        is WhereGeneratedSql -> {
            val actions = wherePart.actions
            val generator = SqlGenerator(
                SqlGenerator.Usage.where,
                paramsIndexOffset,
                outParams,
                paramPlaceholder,
                wherePart.column
            )

            generator.actions()
            generator.generatedSql
        }
        else -> throw IllegalArgumentException("Unknown where part $wherePart")
    }