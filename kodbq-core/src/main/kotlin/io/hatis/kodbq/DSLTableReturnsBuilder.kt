package io.hatis.kodbq

class DSLTableReturnsBuilder(dialect: SqlDialect, tableName: String): DSLReturnsBuilder(dialect, tableName) {
    fun table(tableName: String, builderActions: DSLReturnsBuilder.() -> Unit) {
        DSLReturnsBuilder(dialect, tableName, functions, columns).builderActions()
    }

    internal fun createReturns() = SelectBuilder.Returns(columns, functions)
}