package io.hatis.db

class DSLAggregationBuilder(mode: SqlMode) : DSLAggregationFunctionsBuilder(mode) {
    fun table(tableName: String, builderActions: DSLAggregationFunctionsBuilder.() -> Unit) {
        DSLAggregationFunctionsBuilder(mode, tableName, functions, columns).builderActions()
    }

    internal fun createAggregation() = SelectBuilder.Aggregation(columns, functions)
}