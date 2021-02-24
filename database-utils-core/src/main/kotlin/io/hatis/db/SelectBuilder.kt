package io.hatis.db

class SelectBuilder(
    val tableName: String,
    val columns: Set<String>,
    val where: WherePart?,
    val sort: Sort? = null,
    val limit: Limit? = null,
    val distinct: Boolean = false,
    val aggregation: Aggregation? = null
) {
    data class Sort(val column: String, val asc: Boolean = true)
    data class Limit(val offset: Int = 0, val count: Int = 0)

    interface AggregationFunction
    data class ColumnFunction(val function: String, val column: String): AggregationFunction
    data class DBFunction(val function: String): AggregationFunction
    data class Aggregation(val groupBy: Collection<String>, val functions: Map<String, AggregationFunction>)
}