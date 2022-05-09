package io.hatis.kodbq

open class DSLFunctionsBuilder(
    protected val dialect: SqlDialect,
    protected val tableName: String,
    protected val functions: MutableMap<String, Function> = mutableMapOf(),
) {
    fun count(alias: String) = count( "*", alias)
    fun count(column: String, alias: String) = function("count", column, alias)
    fun max(column: String, alias: String) = function("max", column, alias)
    fun min(column: String, alias: String) = function("min", column, alias)
    fun avg(column: String, alias: String) = function("avg", column, alias)
    fun sum(column: String, alias: String) = function("sum", column, alias)

    fun function(function: String, column: String, alias: String) {
        functions[alias] = SimpleFunction(function, Column(column, dialect, tableName, alias))
    }

    fun native(alias: String, actions: NativeSql.Generator.() -> String) {
        functions[alias] = NativeFunction(NativeSql(dialect, actions))
    }
}