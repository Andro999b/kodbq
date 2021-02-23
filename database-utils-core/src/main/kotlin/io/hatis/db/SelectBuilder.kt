package io.hatis.utils.db.io.hatis

class SelectBuilder(
    val tableName: String,
    val columns: Set<String>,
    val where: WherePart?,
    val sort: Sort? = null,
    val limit: Limit? = null,
) {
    data class Sort(val column: String, val asc: Boolean = true)
    data class Limit(val offset: Int = 0, val count: Int = 0)
}