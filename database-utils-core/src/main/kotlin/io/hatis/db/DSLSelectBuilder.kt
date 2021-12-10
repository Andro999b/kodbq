package io.hatis.db

class DSLSelectBuilder(private val tableName: String, private val mode: SqlMode) {
    private var dslConditionBuilder: DSLSelectConditionBuilder? = null
    private var dslAggregationBuilder: DSLAggregationBuilder? = null
    private var columns: MutableSet<Column> = mutableSetOf()
    private val joins: MutableList<SelectBuilder.Join> = mutableListOf()
    private var sort: Collection<SelectBuilder.Sort> = mutableSetOf()
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false

    fun distinct() {
        distinct = true
    }

    fun returns(columns: Set<String>) {
        this.columns.addAll(columns.map { Column(it, mode, tableName) })
    }

    fun returnsFrom(tableName: String, columns: Set<String>) {
        this.columns.addAll(columns.map { Column(it, mode, tableName) })
    }

    fun returnsFrom(tableName: String, vararg columns: Pair<String, String>) {
        this.columns.addAll(columns.map { Column(it.first, mode, tableName, alias = it.second) })
    }

    fun returns(vararg columns: String) {
        this.columns.addAll(columns.toSet().map { Column(it, mode, tableName) })
    }

    fun returns(vararg columns: Pair<String, String>) {
        this.columns.addAll(columns.map { Column(it.first, mode, tableName, alias = it.second) })
    }

    fun returnsFrom(tableName: String, vararg columns: String) {
        this.columns.addAll(columns.toSet().map { Column(it, mode, tableName) })
    }

    fun where(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLSelectConditionBuilder(mode, tableName)
        dslConditionBuilder.builderActions()

        this.dslConditionBuilder = dslConditionBuilder
    }

    fun join(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, mode, joinTable), tableName),
            SelectBuilder.JoinMode.INNER,
            builderActions
        )
    }

    fun leftJoin(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, mode, joinTable), tableName),
            SelectBuilder.JoinMode.LEFT,
            builderActions
        )
    }


    fun rightJoin(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, mode, joinTable), tableName),
            SelectBuilder.JoinMode.RIGHT,
            builderActions
        )
    }


    fun fullJoin(joinTable: String, joinColumn: String, builderActions: DSLJoinBuilder.() -> Unit) {
        addJoin(
            DSLJoinBuilder(Column(joinColumn, mode, joinTable), tableName),
            SelectBuilder.JoinMode.FULL,
            builderActions
        )
    }

    private fun addJoin(
        builder: DSLJoinBuilder,
        joinMode: SelectBuilder.JoinMode,
        builderActions: DSLJoinBuilder.() -> Unit,
    ) {
        builder.builderActions()
        joins.add(builder.createJoin(joinMode))
    }


    fun aggregation(builderActions: DSLAggregationBuilder.() -> Unit) {
        val dslAggregationBuilder = DSLAggregationBuilder(mode)
        dslAggregationBuilder.builderActions()
        this.dslAggregationBuilder = dslAggregationBuilder
    }

    fun sort(columnName: String, asc: Boolean = true) {
        this.sort += setOf(SelectBuilder.Sort(Column(columnName, mode), asc))
    }

    fun sort(columns: Collection<String>, asc: Boolean = true) {
        this.sort += columns.map { SelectBuilder.Sort( Column(it, mode), asc) }.toSet()
    }

    fun sort(tableName: String, columnName: String, asc: Boolean = true) {
        this.sort += setOf(SelectBuilder.Sort(Column(columnName, mode, tableName), asc))
    }

    fun sort(tableColumns: Map<String, String>, asc: Boolean = true) {
        this.sort += tableColumns.entries.map { SelectBuilder.Sort(Column(it.value, mode, table = it.key), asc) }.toSet()
    }

    fun sort(sortColumns: Collection<SelectBuilder.Sort>) {
        this.sort += sortColumns
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
        columns = columns,
        joins = joins,
        where = dslConditionBuilder?.createWhereCondition(),
        limit = limit,
        sort = sort,
        distinct = distinct,
        aggregation = dslAggregationBuilder?.createAggregation(),
        mode = mode
    )
}