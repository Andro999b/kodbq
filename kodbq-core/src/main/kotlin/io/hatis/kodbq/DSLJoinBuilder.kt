package io.hatis.kodbq

class DSLJoinBuilder(private val joinColumn: Column, private val onTable: String) {
    private lateinit var onColumn: Column
    private var alias: String? = null

    fun on(columnName: String) {
        onColumn = Column(columnName, joinColumn.dialect, onTable)
    }

    fun on(tableName: String, columnName: String) {
        onColumn = Column(columnName, joinColumn.dialect, tableName)
    }

    internal fun createJoin(joinMode: SelectBuilder.JoinMode) = SelectBuilder.Join(
        joinColumn,
        onColumn,
        joinMode,
        alias = alias
    )
}