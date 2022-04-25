package io.hatis.kodbq

enum class WhereOps(val op: String) {
    EQ("="),
    NEQ("!="),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    LIKE("like"),
    IN("in")
}

interface WherePart
abstract class WhereJoint(val separator: String) : WherePart {
    val parts: MutableList<WherePart> = mutableListOf()
}

data class WhereColumn(val column: Named, val op: WhereOps, val params: Any, val dialect: SqlDialect) : WherePart

data class WhereGeneratedSql(val nativeSql: NativeSqlColumn) : WherePart
data class WhereColumnIsNull(val column: Named) : WherePart
data class WhereColumnIsNotNull(val column: Named) : WherePart
class Or : WhereJoint("or")
class And : WhereJoint("and")

private class WhereBuilder(
    private val outParams: MutableList<Any?>,
    private val paramPlaceholder: (Int) -> String,
    private val paramsIndexOffset: Int = 0
) {
    private val builder = StringBuilder()

    fun build(wherePart: WherePart): String {
        buildRecursive(wherePart)
        return builder.toString()
    }

    private fun buildRecursive(wherePart: WherePart, depth: Int = 0) {
        when (wherePart) {
            is WhereJoint ->
                when {
                    wherePart.parts.size == 1 ->
                        buildRecursive(wherePart.parts.first(), depth + 1)
                    wherePart.parts.size > 1 -> {
                        if (depth != 0) builder.append("(")
                        var lastLength = builder.length
                        wherePart.parts.forEachIndexed { index, p ->
                            if (index != 0 && lastLength != builder.length) {
                                builder.append(" ").append(wherePart.separator).append(" ")
                                lastLength = builder.length
                            }
                            buildRecursive(p, depth + 1)
                        }
                        if (depth != 0) builder.append(")")
                    }
                }
            is WhereColumn -> {
                when (wherePart.op) {
                    WhereOps.IN -> { // expand in params
                        when (wherePart.dialect) {
                            SqlDialect.PG -> {
                                outParams.add(wherePart.params)
                                builder
                                    .append(wherePart.column.escapeName())
                                    .append(" = any(")
                                    .append(paramPlaceholder(outParams.size + paramsIndexOffset))
                                    .append(")")
                            }
                            else -> {
                                outParams.add(wherePart.params)
                                builder
                                    .append(wherePart.column.escapeName())
                                    .append(" in(")
                                    .append(paramPlaceholder(outParams.size + paramsIndexOffset))
                                    .append(")")
                            }
                        }
                    }
                    else -> {
                        outParams.add(wherePart.params)
                        val column = wherePart.column.escapeName()
                        val placeholder = paramPlaceholder(outParams.size + paramsIndexOffset)
                        builder
                            .append(column)
                            .append(" ")
                            .append(wherePart.op.op)
                            .append(" ")
                            .append(placeholder)
                    }
                }
            }
            is WhereColumnIsNotNull -> builder.append(wherePart.column.escapeName()).append(" is not null")
            is WhereColumnIsNull -> builder.append(wherePart.column.escapeName()).append(" is null")
            is WhereGeneratedSql -> {
                builder.append(
                    wherePart.nativeSql.generate(
                        NativeSqlColumn.Usage.CONDITION,
                        paramsIndexOffset,
                        outParams,
                        paramPlaceholder,
                    )
                )
            }
        }
    }
}

internal fun buildWhere(
    wherePart: WherePart,
    outParams: MutableList<Any?>,
    paramPlaceholder: (Int) -> String,
    paramsIndexOffset: Int = 0
): String = WhereBuilder(outParams, paramPlaceholder, paramsIndexOffset).build(wherePart)
