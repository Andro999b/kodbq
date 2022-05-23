package io.hatis.kodbq

import java.lang.IllegalArgumentException

enum class WhereOps(val op: String) {
    EQ("="),
    NEQ("!="),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    LIKE(" like "),
    IN(" in ")
}

interface WherePart
abstract class WhereJoint(val separator: String) : WherePart {
    val parts: MutableList<WherePart> = mutableListOf()
}

data class WhereColumn(val column: Named, val op: WhereOps, val value: Any, val dialect: SqlDialect) : WherePart

data class WhereGeneratedSql(val nativeSql: NativeSql) : WherePart
data class WhereColumnIsNull(val column: Named) : WherePart
data class WhereColumnIsNotNull(val column: Named) : WherePart
class Or : WhereJoint("or")
class And : WhereJoint("and")

internal class WhereBuilder(
    private val buildOptions: BuildOptions,
    private val paramPlaceholder: (Int) -> String,
    private val outParams: MutableList<Any?> = mutableListOf(),
    private val paramsIndexOffset: Int = 0
) {
    private val builder = StringBuilder()

    fun build(wherePart: WherePart): String {
        buildRecursive(wherePart)
        return builder.toString()
    }

    private fun buildRecursive(wherePart: WherePart, depth: Int = 0) {
        when (wherePart) {
            is WhereJoint -> buildConditionsJoin(wherePart, depth)
            is WhereColumn -> buildColumnCondition(wherePart)
            is WhereColumnIsNotNull -> builder.append(wherePart.column.sql).append(" is not null")
            is WhereColumnIsNull -> builder.append(wherePart.column.sql).append(" is null")
            is WhereGeneratedSql -> {
                builder.append(
                    wherePart.nativeSql.generate(
                        paramsIndexOffset,
                        outParams,
                        paramPlaceholder,
                    )
                )
            }
        }
    }

    private fun buildColumnCondition(wherePart: WhereColumn) {
        when (wherePart.op) {
            WhereOps.IN -> buildInPart(wherePart)
            else -> {
                outParams.add(wherePart.value)
                val column = wherePart.column.sql
                val placeholder = paramPlaceholder(outParams.size + paramsIndexOffset)
                builder
                    .append(column)
                    .append(wherePart.op.op)
                    .append(placeholder)
            }
        }
    }

    private fun buildConditionsJoin(wherePart: WhereJoint, depth: Int) {
        when {
            wherePart.parts.size == 1 -> buildRecursive(wherePart.parts.first(), depth + 1)
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
    }

    private fun buildInPart(wherePart: WhereColumn) {
        val value = when (wherePart.value) {
            is Collection<*> -> wherePart.value.toTypedArray()
            is Array<*> -> wherePart.value
            else -> throw IllegalArgumentException("Expected collection or array for IN operation")
        }

        if(buildOptions.expandIn) {
            builder
                .append(wherePart.column.sql)
                .append(" in(")

            value.forEach {
                outParams.add(it)
                builder.append(paramPlaceholder(outParams.size + paramsIndexOffset))
                builder.append(",")
            }

            builder.replace(builder.length - 1, builder.length, ")")
            return
        }

        when (wherePart.dialect) {
            SqlDialect.PG -> {
                outParams.add(value)
                builder
                    .append(wherePart.column.sql)
                    .append("=any(")
                    .append(paramPlaceholder(outParams.size + paramsIndexOffset))
                    .append(")")
            }
            else -> {
                outParams.add(value)
                builder
                    .append(wherePart.column.sql)
                    .append(" in(")
                    .append(paramPlaceholder(outParams.size + paramsIndexOffset))
                    .append(")")
            }
        }
    }
}
