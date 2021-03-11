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
interface WhereJoint: WherePart {
    val parts: Collection<WherePart>
    val separator: String
}
data class WhereColumn(val column: Column, val op: WhereOps, val params: Any): WherePart
data class WhereColumnIsNull(val column: Column): WherePart
data class WhereColumnIsNotNull(val column: Column): WherePart
data class Or(override val parts: Collection<WherePart>): WhereJoint {
    override val separator: String
        get() = " or "
}
data class And(override val parts: Collection<WherePart>): WhereJoint {
    override val separator: String
        get() = " and "
}
internal fun buildWhere(
        wherePart: WherePart,
        outParams: MutableList<Any>,
        escape: (String) -> String,
        paramPlaceholder: (Int) -> String,
        paramsIndexOffset: Int = 0
): String =
        when (wherePart) {
            is WhereJoint ->
                when {
                    wherePart.parts.size == 1 ->
                        buildWhere(wherePart.parts.first(), outParams, escape,paramPlaceholder, paramsIndexOffset)
                    wherePart.parts.size > 1 ->
                        "(" + wherePart.parts.joinToString(wherePart.separator) {
                            buildWhere(it, outParams, escape, paramPlaceholder, paramsIndexOffset)
                        } + ")"
                    else -> ""
                }
            is WhereColumn -> {
                outParams.add(wherePart.params)
                val column = wherePart.column
                val placeholder = paramPlaceholder(outParams.size + paramsIndexOffset)
                when (wherePart.op) {
                    WhereOps.like -> "$column like $placeholder"
                    WhereOps.`in` -> "$column in ($placeholder)"
                    else -> "$column ${wherePart.op.op} $placeholder"
                }
            }
            is WhereColumnIsNotNull -> "${wherePart.column} is not null"
            is WhereColumnIsNull -> "${wherePart.column} is null"
            else -> throw IllegalArgumentException("Unknown where part $wherePart")
        }