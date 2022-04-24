package io.hatis.kodbq

class DSLSelectBuilder(private val tableName: String, private val dialect: SqlDialect) {
    private var dslConditionBuilder: DSLSelectConditionBuilder? = null
    private var dslReturnsBuilder: DSLHierarchyReturnsBuilder? = null
    private val joins: MutableList<SelectBuilder.Join> = mutableListOf()
    private var sort: MutableSet<SelectBuilder.Sort> = mutableSetOf()
    private var groupByColumns: MutableSet<Column> = mutableSetOf()
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false

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

    fun join(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, dialect, joinTable), tableName),
            SelectBuilder.JoinMode.INNER,
            builderActions
        )
    }

    fun leftJoin(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, dialect, joinTable), tableName),
            SelectBuilder.JoinMode.LEFT,
            builderActions
        )
    }


    fun rightJoin(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, dialect, joinTable), tableName),
            SelectBuilder.JoinMode.RIGHT,
            builderActions
        )
    }


    fun fullJoin(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, dialect, joinTable), tableName),
            SelectBuilder.JoinMode.FULL,
            builderActions
        )
    }

    private fun addJoin(
        builder: DSLJoinBuilder,
        JoinMode: SelectBuilder.JoinMode,
        builderActions: DSLJoinBuilder.() -> Unit,
    ) {
        builder.builderActions()
        joins.add(builder.createJoin(JoinMode))
    }

    fun sort(columnName: String, asc: Boolean = true) {
        this.sort += SelectBuilder.Sort(Column(columnName, dialect), asc)
    }

    fun sort(columns: Collection<String>, asc: Boolean = true) {
        this.sort += columns.map { SelectBuilder.Sort(Column(it, dialect), asc) }.toSet()
    }

    fun sort(tableName: String, columnName: String, asc: Boolean = true) {
        this.sort += SelectBuilder.Sort(Column(columnName, dialect, tableName), asc)
    }

    fun sort(tableName: String, columns: Collection<String>, asc: Boolean = true) {
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
        columnNames.forEach {
            groupByColumns += Column(it, dialect, tableName)
        }
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
}