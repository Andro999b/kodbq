package io.hatis.kodbq

class DSLTableSortBuilder(dialect: SqlDialect, tableName: String): DSLSortBuilder(dialect, tableName) {
    fun table(tableName: String, builderActions: DSLSortBuilder.() -> Unit) {
        DSLSortBuilder(dialect, tableName, sortColumns).builderActions()
    }
}