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
interface Join: WherePart {
    val parts: Collection<WherePart>
    val separator: String
}
data class WhereColumn(val column: Column, val op: WhereOps, val params: Any): WherePart
data class WhereColumnIsNull(val column: Column): WherePart
data class WhereColumnIsNotNull(val column: Column): WherePart
data class Or(override val parts: Collection<WherePart>): Join {
    override val separator: String
        get() = " or "
}
data class And(override val parts: Collection<WherePart>): Join {
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
            is Join ->
                if(wherePart.parts.size == 1) {
                    buildWhere(wherePart.parts.first(), outParams, escape,paramPlaceholder, paramsIndexOffset)
                } else {
                    "(" + wherePart.parts.joinToString(wherePart.separator) {
                        buildWhere(it, outParams, escape, paramPlaceholder, paramsIndexOffset)
                    } + ")"
                }
            is WhereColumn -> {
                outParams.add(wherePart.params)
                when (wherePart.op) {
                    WhereOps.like -> "${wherePart.column} like ${paramPlaceholder(outParams.size + paramsIndexOffset)}"
                    WhereOps.`in` -> "${wherePart.column} in (${paramPlaceholder(outParams.size + paramsIndexOffset)})"
                    else -> "${wherePart.column}  ${wherePart.op.op} ${paramPlaceholder(outParams.size + paramsIndexOffset)}"
                }
            }
            is WhereColumnIsNotNull -> "${wherePart.column}  is not null"
            is WhereColumnIsNull -> "${wherePart.column}  is null"
            else -> throw IllegalArgumentException("Unknown where part $wherePart")
        }