package io.hatis.db

class DSLJoinBuilder(private val joinColumn: Column, private val onTable: String) {
    private lateinit var onColumn: Column
    private var alias: String? = null

    fun on(columnName: String) {
        onColumn = Column(columnName, joinColumn.mode, onTable)
    }

    fun on(tableName: String, columnName: String) {
        onColumn = Column(columnName, joinColumn.mode, tableName)
    }

    internal fun createJoin(joinMode: SelectBuilder.JoinMode) = SelectBuilder.Join(
        joinColumn,
        onColumn,
        joinMode,
        alias = alias
    )
}