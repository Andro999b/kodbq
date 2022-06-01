package io.hatis.kodbq

class DSLReturnsBuilder(private val dialect: SqlDialect) {

    private val functions: MutableMap<String, Function> = mutableMapOf()
    private val columns: MutableSet<Column> = mutableSetOf()

    fun count(alias: String) {
        functions[alias] = SimpleFunction("count", Column("*", dialect, alias = alias))
    }
    fun count(c: ColumnDefinition, alias: String) = function("count", c, alias)
    fun max(c: ColumnDefinition, alias: String) = function("max", c, alias)
    fun min(c: ColumnDefinition, alias: String) = function("min", c, alias)
    fun avg(c: ColumnDefinition, alias: String) = function("avg", c, alias)
    fun sum(c: ColumnDefinition, alias: String) = function("sum", c, alias)

    fun function(function: String, c: ColumnDefinition, alias: String) {
        functions[alias] = SimpleFunction(function, c.toColunm(dialect, alias))
    }

    fun native(alias: String, actions: NativeSql.Generator.() -> String) {
        functions[alias] = NativeFunction(NativeSql(dialect, actions))
    }

    fun column(cd: ColumnDefinition) {
        columns += cd.toColunm(dialect)
    }

    fun column(cd: ColumnDefinition, alias: String) {
        columns += cd.toColunm(dialect, alias)
    }

    fun columns(vararg cds: ColumnDefinition) {
        columns.addAll(cds.toSet().map { it.toColunm(dialect) })
    }

    fun columns(cds: Set<ColumnDefinition>) {
        columns.addAll(cds.map { it.toColunm(dialect) })
    }

    fun columns(vararg cds: Pair<ColumnDefinition, String>) {
        columns.addAll(cds.map { it.first.toColunm(dialect, alias = it.second) })
    }
    internal fun createReturns() = SelectBuilder.Returns(columns, functions)
}