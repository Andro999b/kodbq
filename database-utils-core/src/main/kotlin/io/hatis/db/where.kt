package io.hatis.utils.db.io.hatis

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
data class Column(val columnName: String, val op: WhereOps, val params: Any): WherePart
data class ColumnIsNull(val columnName: String): WherePart
data class ColumnIsNotNull(val columnName: String): WherePart
data class Or(override val parts: Collection<WherePart>): Join {
    override val separator: String
        get() = " or "
}
data class And(override val parts: Collection<WherePart>): Join {
    override val separator: String
        get() = " and "
}
