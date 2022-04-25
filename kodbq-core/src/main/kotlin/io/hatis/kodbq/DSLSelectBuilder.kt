package io.hatis.kodbq

class DSLSelectBuilder(private val tableName: String, private val dialect: SqlDialect) {
    private var dslConditionBuilder: DSLSelectConditionBuilder? = null
    private var dslReturnsBuilder: DSLHierarchyReturnsBuilder? = null
    private val joins: MutableList<SelectBuilder.Join> = mutableListOf()
    private var sort: MutableSet<SelectBuilder.Sort> = mutableSetOf()
    private var groupByColumns: MutableSet<Column> = mutableSetOf()
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false
    private val joinBuilder = JoinBuilder(tableName, joins)

    fun distinct() {
        distinct = true
    }

    fun returns(builderActions: DSLHierarchyReturnsBuilder.() -> Unit) {
        val dslReturnsBuilder = DSLHierarchyReturnsBuilder(dialect, tableName)
        dslReturnsBuilder.builderActions()
        this.dslReturnsBuilder = dslReturnsBuilder
    }

    fun where(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLSelectConditionBuilder(dialect, tableName)
        dslConditionBuilder.builderActions()

        this.dslConditionBuilder = dslConditionBuilder
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


    fun sort(columnName: String, asc: Boolean = true) {
        sortByTable(tableName, columnName, asc)
    }

    fun sort(columns: Collection<String>, asc: Boolean = true) {
        sortByTable(tableName, columns, asc)
    }

    fun sortByTable(tableName: String, columnName: String, asc: Boolean = true) {
        this.sort += SelectBuilder.Sort(Column(columnName, dialect, tableName), asc)
    }

    fun sortByTable(tableName: String, columns: Collection<String>, asc: Boolean = true) {
        this.sort += columns.map { SelectBuilder.Sort(Column(it, dialect, tableName), asc) }.toSet()
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

    fun groupBy(vararg columnNames: String) {
        groupByTable(tableName, *columnNames)
    }

    fun groupByTable(tableName: String, vararg columnNames: String) {
        columnNames.forEach {
            groupByColumns += Column(it, dialect, tableName)
        }
    }

    internal fun createSelectBuilder() = SelectBuilder(
        tableName = tableName,
        joins = joins,
        where = dslConditionBuilder?.createWhereCondition(),
        limit = limit,
        sort = sort,
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