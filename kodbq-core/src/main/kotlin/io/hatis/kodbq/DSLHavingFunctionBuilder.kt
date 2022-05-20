package io.hatis.kodbq

open class DSLHavingFunctionBuilder(dialect: SqlDialect, tableName: String?, andJoint: And = And()) :
    DSLConditionBuilder(dialect, tableName, andJoint) {
    fun count() = count("*")
    fun count(columnName: String) = function("count", columnName)
    fun max(columnName: String) = function("max", columnName)
    fun min(columnName: String) = function("min", columnName)
    fun avg(columnName: String) = function("avg", columnName)
    fun sum(columnName: String) = function("sum", columnName)

    fun function(function: String, columnName: String): ColumnConditionBuilder {
        return columnConditionBuilder.apply {
            column = SimpleFunction(function, Column(columnName, dialect, tableName))
        }
    }
}