package io.hatis.kodbq

open class DSLHavingFunctionBuilder(dialect: SqlDialect): DSLConditionBuilder(dialect) {
    fun count() = FunctionCondition(SimpleFunction("count", Column("*", dialect)))
    fun count(c: ColumnDefinition) = function("count", c)
    fun max(c: ColumnDefinition) = function("max", c)
    fun min(c: ColumnDefinition) = function("min", c)
    fun avg(c: ColumnDefinition) = function("avg", c)
    fun sum(c: ColumnDefinition) = function("sum", c)

    fun function(function: String, c: ColumnDefinition) = FunctionCondition(SimpleFunction(function, c.toColumn(dialect)))

    class FunctionCondition(private val simpleFunction: SimpleFunction): Conditionable {
        override fun toConditionName(dialect: SqlDialect) = simpleFunction
    }
}