package io.hatis.db

open class DSLAggregationFunctionsBuilder(
    protected val mode: SqlMode,
    private val tableName: String? = null,
    protected val functions: MutableMap<String, SelectBuilder.AggregationFunction> = mutableMapOf(),
    protected val columns: MutableSet<Column> = mutableSetOf()
) {
    fun groupBy(vararg columnNames: String) {
        columnNames.forEach {
            columns.add(Column(it, mode, tableName))
        }
    }

    fun count(alias: String) = columnFunction("count", "*", alias)
    fun count(column: String, alias: String) = columnFunction("count", column, alias)
    fun max(column: String, alias: String) = columnFunction("max", column, alias)
    fun min(column: String, alias: String) = columnFunction("min", column, alias)
    fun avg(column: String, alias: String) = columnFunction("avg", column, alias)
    fun sum(column: String, alias: String) = columnFunction("sum", column, alias)

    fun function(function: String, alias: String) {
        functions[alias] = SelectBuilder.DBFunction(function)
    }

    private fun columnFunction(function: String, column: String, alias: String) {
        functions[alias] = SelectBuilder.ColumnFunction(function, Column(column, mode, tableName))
    }
}