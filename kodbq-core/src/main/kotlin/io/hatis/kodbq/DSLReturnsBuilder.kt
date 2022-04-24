package io.hatis.kodbq

open class DSLReturnsBuilder(
    dialect: SqlDialect,
    tableName: String? = null,
    functions: MutableMap<String, Function> = mutableMapOf(),
    protected val columns: MutableSet<Column> = mutableSetOf()
): DSLFunctionsBuilder(dialect, tableName, functions) {
    fun columns(columns: Set<String>) {
        this.columns.addAll(columns.map { Column(it, dialect, tableName) })
    }

    fun columns(vararg columns: String) {
        this.columns.addAll(columns.toSet().map { Column(it, dialect, tableName) })
    }

    fun columns(vararg columns: Pair<String, String>) {
        this.columns.addAll(columns.map { Column(it.first, dialect, tableName, alias = it.second) })
    }
}