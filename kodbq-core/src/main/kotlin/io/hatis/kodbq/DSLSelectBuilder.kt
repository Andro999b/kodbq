package io.hatis.kodbq

class DSLSelectBuilder(private val tableName: String, private val dialect: SqlDialect) {
    private var dslConditionBuilder: DSLSelectConditionBuilder? = null
    private var dslReturnsBuilder: DSLTableReturnsBuilder? = null
    private var dslHavingBuilder: DSLHavingBuilder? = null
    private val joins: MutableList<SelectBuilder.Join> = mutableListOf()
    private var dslSortBuilder: DSLTableSortBuilder? = null
    private var groupByColumns: MutableSet<Column> = mutableSetOf()
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false
    private val joinBuilder = JoinBuilder(tableName, joins)

    fun distinct() {
        distinct = true
    }

    fun returns(builderActions: DSLTableReturnsBuilder.() -> Unit) {
        val dslReturnsBuilder = DSLTableReturnsBuilder(dialect, tableName)
        dslReturnsBuilder.builderActions()
        this.dslReturnsBuilder = dslReturnsBuilder
    }

    fun join(joinTable: String, joinColumnName: String) = joinBuilder.apply {
        joinColumn = Column(joinColumnName, dialect, joinTable)
        joinMode = SelectBuilder.JoinMode.INNER
    }

    fun leftJoin(joinTable: String, joinColumnName: String) = joinBuilder.apply {
        joinColumn = Column(joinColumnName, dialect, joinTable)
        joinMode = SelectBuilder.JoinMode.LEFT
    }


    fun rightJoin(joinTable: String, joinColumnName: String) = joinBuilder.apply {
        joinColumn = Column(joinColumnName, dialect, joinTable)
        joinMode = SelectBuilder.JoinMode.RIGHT
    }


    fun fullJoin(joinTable: String, joinColumnName: String) = joinBuilder.apply {
        joinColumn = Column(joinColumnName, dialect, joinTable)
        joinMode = SelectBuilder.JoinMode.FULL
    }

    fun where(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLSelectConditionBuilder(dialect, tableName)
        dslConditionBuilder.builderActions()

        this.dslConditionBuilder = dslConditionBuilder
    }


    fun groupBy(vararg columnNames: String) {
        groupByTable(tableName, *columnNames)
    }

    fun groupByTable(tableName: String, vararg columnNames: String) {
        columnNames.forEach {
            groupByColumns += Column(it, dialect, tableName)
        }
    }

    fun having(builderActions: DSLHavingBuilder.() -> Unit) {
        val dslHavingBuilder = DSLHavingBuilder(dialect, tableName)
        dslHavingBuilder.builderActions()
        this.dslHavingBuilder = dslHavingBuilder
    }

    fun sort(builderActions: DSLTableSortBuilder.() -> Unit) {
        val dslSortBuilder = DSLTableSortBuilder(dialect, tableName)
        dslSortBuilder.builderActions()
        this.dslSortBuilder = dslSortBuilder
    }

    fun limit(count: Int) {
        this.limit = this.limit?.copy(count = count) ?: SelectBuilder.Limit(count = count)
    }

    fun offset(offset: Int) {
        this.limit = this.limit?.copy(offset = offset) ?: SelectBuilder.Limit(offset)
    }

    fun range(from: Int, to: Int) {
        if (to < from) {
            throw IllegalArgumentException("to < offset")
        }
        this.limit = SelectBuilder.Limit(from, to - from)
    }

    internal fun createSelectBuilder() = SelectBuilder(
        tableName = tableName,
        joins = joins,
        where = dslConditionBuilder?.createWhereCondition(),
        having = dslHavingBuilder?.createWhereCondition(),
        limit = limit,
        sort = dslSortBuilder?.sortColumns ?: emptySet(),
        distinct = distinct,
        returns = dslReturnsBuilder?.createReturns() ?: SelectBuilder.Returns(),
        groupByColumns = groupByColumns,
        dialect = dialect
    )

    class JoinBuilder(private val tableName: String, private val joins: MutableList<SelectBuilder.Join>) {
        internal lateinit var joinColumn: Column
        internal lateinit var joinMode: SelectBuilder.JoinMode

        infix fun on(columnName: String) = on(tableName, columnName)

        fun on(tableName: String, columnName: String) {
            joins += SelectBuilder.Join(
                joinColumn,
                Column(columnName, joinColumn.dialect, tableName),
                joinMode
            )
        }
    }
}