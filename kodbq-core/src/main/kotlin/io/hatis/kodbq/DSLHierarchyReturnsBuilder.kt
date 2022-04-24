package io.hatis.kodbq

class DSLHierarchyReturnsBuilder(mode: SqlDialect, tableName: String? = null) : DSLReturnsBuilder(mode, tableName) {
    fun table(tableName: String, builderActions: DSLReturnsBuilder.() -> Unit) {
        DSLReturnsBuilder(dialect, tableName, functions, columns).builderActions()
    }

    internal fun createReturns() = SelectBuilder.Returns(columns, functions)
}