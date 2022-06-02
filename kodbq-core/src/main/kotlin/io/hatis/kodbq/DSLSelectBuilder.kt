package io.hatis.kodbq

open class DSLSelectBuilder(
    protected val table: Table,
    protected val dialect: SqlDialect
) {
    private var dslConditionBuilder: DSLSelectConditionBuilder? = null
    private var dslReturnsBuilder: DSLReturnsBuilder? = null
    private var dslHavingBuilder: DSLHavingBuilder? = null
    private val joins: MutableList<SelectBuilder.Join> = mutableListOf()
    private var dslSortBuilder: DSLSortBuilder? = null
    private var groupByColumns: MutableSet<Column> = mutableSetOf()
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false

    fun distinct() {
        distinct = true
    }

    fun returns(builderActions: DSLReturnsBuilder.() -> Unit) {
        val dslReturnsBuilder = DSLReturnsBuilder(dialect)
        dslReturnsBuilder.builderActions()
        this.dslReturnsBuilder = dslReturnsBuilder
    }

    infix fun ColumnDefinition.joinOn(refCol: ColumnDefinition) {
        buildJoin(
            this.toColumn(dialect),
            refCol.toColumn(dialect),
            SelectBuilder.JoinMode.INNER
        )
    }

    infix fun ColumnDefinition.leftJoinOn(refCol: ColumnDefinition) {
        buildJoin(
            this.toColumn(dialect),
            refCol.toColumn(dialect),
            SelectBuilder.JoinMode.LEFT
        )
    }


    infix fun ColumnDefinition.rightJoinOn(refCol: ColumnDefinition) {
        buildJoin(
            this.toColumn(dialect),
            refCol.toColumn(dialect),
            SelectBuilder.JoinMode.RIGHT
        )
    }


    infix fun ColumnDefinition.fullJoinOn(refCol: ColumnDefinition) {
        buildJoin(
            this.toColumn(dialect),
            refCol.toColumn(dialect),
            SelectBuilder.JoinMode.FULL
        )
    }

    fun join(rd: ReferenceDefinition) {
        buildJoin(
            rd.columnDefinition.toColumn(dialect),
            rd.referredColumnDefinition.toColumn(dialect),
            SelectBuilder.JoinMode.INNER
        )
    }

    fun leftJoin(rd: ReferenceDefinition) {
        buildJoin(
            rd.columnDefinition.toColumn(dialect),
            rd.referredColumnDefinition.toColumn(dialect),
            SelectBuilder.JoinMode.LEFT
        )
    }


    fun rightJoin(rd: ReferenceDefinition) {
        buildJoin(
            rd.columnDefinition.toColumn(dialect),
            rd.referredColumnDefinition.toColumn(dialect),
            SelectBuilder.JoinMode.RIGHT
        )
    }


    fun fullJoin(rd: ReferenceDefinition) {
        buildJoin(
            rd.columnDefinition.toColumn(dialect),
            rd.referredColumnDefinition.toColumn(dialect),
            SelectBuilder.JoinMode.FULL
        )
    }

    private fun buildJoin(joinColumn: Column, onColumn: Column, joinMode: SelectBuilder.JoinMode) {
        joins += SelectBuilder.Join(joinColumn, onColumn, joinMode)
    }

    fun where(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLSelectConditionBuilder(dialect)
        dslConditionBuilder.builderActions()

        this.dslConditionBuilder = dslConditionBuilder
    }

    fun groupBy(vararg cds: ColumnDefinition) {
        cds.forEach { groupByColumns += it.toColumn(dialect) }
    }

    fun having(builderActions: DSLHavingBuilder.() -> Unit) {
        val dslHavingBuilder = DSLHavingBuilder(dialect)
        dslHavingBuilder.builderActions()
        this.dslHavingBuilder = dslHavingBuilder
    }

    fun sort(builderActions: DSLSortBuilder.() -> Unit) {
        val dslSortBuilder = DSLSortBuilder(dialect)
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

    internal fun createSelect(unionAll: Boolean) = SelectBuilder.Select(
        table = table,
        joins = joins,
        where = dslConditionBuilder?.createWhereCondition(),
        having = dslHavingBuilder?.createWhereCondition(),
        limit = limit,
        sort = dslSortBuilder?.sortColumns ?: emptySet(),
        distinct = distinct,
        returns = dslReturnsBuilder?.createReturns() ?: SelectBuilder.Returns(),
        groupByColumns = groupByColumns,
        unionAll = unionAll
    )
}