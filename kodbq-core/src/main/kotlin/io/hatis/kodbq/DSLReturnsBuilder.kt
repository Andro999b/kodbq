package io.hatis.kodbq

open class DSLReturnsBuilder(
    dialect: SqlDialect,
    tableName: String,
    functions: MutableMap<String, Function> = mutableMapOf(),
    protected val columns: MutableSet<Column> = mutableSetOf()
): DSLFunctionsBuilder(dialect, tableName, functions) {

    fun column(name: String) {
        columns += Column(name, dialect, tableName)
    }

    fun column(name: String, alias: String) {
        columns += Column(name, dialect, tableName, alias)
    }

    fun columns(names: Set<String>) {
        columns.addAll(names.map { Column(it, dialect, tableName) })
    }

    fun columns(vararg names: String) {
        columns.addAll(names.toSet().map { Column(it, dialect, tableName) })
    }

    fun columns(vararg names: Pair<String, String>) {
        columns.addAll(names.map { Column(it.first, dialect, tableName, alias = it.second) })
    }
}