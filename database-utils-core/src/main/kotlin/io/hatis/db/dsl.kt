package io.hatis.db

fun sqlInsert(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLInsertBuilder.() -> Unit
): InsertBuilder {
    val dslInsertBuilder = DSLInsertBuilder(tableName, mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createInsertBuilder()
}

fun sqlUpdate(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLUpdateBuilder.() -> Unit
): UpdateBuilder {
    val dslInsertBuilder = DSLUpdateBuilder(mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createUpdateBuilder(tableName)
}

fun sqlSelect(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLSelectBuilder.() -> Unit
): SelectBuilder {
    val dslInsertBuilder = DSLSelectBuilder(tableName, mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createSelectBuilder()
}

fun sqlDelete(
    tableName: String,
    mode: SqlMode = SqlMode.PG,
    builderActions: DSLDeleteBuilder.() -> Unit
): DeleteBuilder {
    val dslInsertBuilder = DSLDeleteBuilder(tableName, mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createDeleteBuilder()
}

class DSLDeleteBuilder(
    private val tableName: String,
    private val mode: SqlMode
) {
    private lateinit var dslConditionBuilder: DSLUpdateConditionBuilder

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        dslConditionBuilder = DSLUpdateConditionBuilder(mode)
        dslConditionBuilder.builderActions()
    }

    internal fun createDeleteBuilder() = DeleteBuilder(
        tableName = tableName,
        where = dslConditionBuilder.createWhereCondition(),
        mode = mode
    )
}

class DSLMutationColumnsBuilder(private val mode: SqlMode) {
    internal val columns: MutableMap<Column, Any?> = mutableMapOf()

    fun columns(params: Map<String, Any?>) {
        columns.putAll(params.mapKeys { (columnName, _) -> Column(columnName, mode) })
    }

    fun columns(vararg pairs: Pair<String, Any?>) {
        pairs.forEach { column(it.first, it.second) }
    }

    fun column(columnName: String, value: Any?) {
        columns[Column(columnName, mode)] = value
    }

    fun column(columnName: Enum<*>, value: Any?) {
        columns[Column(columnName.name, mode)] = value
    }

    fun generate(columnName: String, actions: SqlGenerator.() -> Unit) {
        column(columnName, SqlGenerator.GeneratedPart(actions))
    }
}

class DSLInsertBuilder(
    private val tableName: String,
    private val mode: SqlMode
) {
    private var values: MutableList<Map<Column, Any?>> = mutableListOf()
    private var genKeys: Set<String> = emptySet()

    fun values(builderActions: DSLMutationColumnsBuilder.() -> Unit) {
        val dslColumnsBuilder = DSLMutationColumnsBuilder(mode)
        dslColumnsBuilder.builderActions()
        values.add(dslColumnsBuilder.columns)
    }

    fun generatedKeys(vararg keys: String) {
        genKeys = keys.toSet()
    }

    internal fun createInsertBuilder() = InsertBuilder(
        tableName = tableName,
        values = values,
        generatedKeys = genKeys,
        mode = mode
    )
}

class DSLUpdateBuilder(private val mode: SqlMode) {
    private lateinit var dslColumnsBuilder: DSLMutationColumnsBuilder
    private var dslConditionBuilder: DSLUpdateConditionBuilder? = null

    fun set(builderActions: DSLMutationColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLMutationColumnsBuilder(mode)
        dslColumnsBuilder.builderActions()
    }

    fun where(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLUpdateConditionBuilder(mode)
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createUpdateBuilder(tableName: String) = UpdateBuilder(
        tableName = tableName,
        columns = dslColumnsBuilder.columns,
        where = dslConditionBuilder?.createWhereCondition(),
        mode = mode
    )
}

class DSLSelectBuilder(private val tableName: String, private val mode: SqlMode) {
    private var dslConditionBuilder: DSLSelectConditionBuilder? = null
    private var dslAggregationBuilder: DSLAggregationBuilder? = null
    private var columns: MutableSet<Column> = mutableSetOf()
    private val joins: MutableList<SelectBuilder.Join> = mutableListOf()
    private var sort: SelectBuilder.Sort? = null
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
        this.sort = SelectBuilder.Sort(setOf(Column(columnName, mode)), asc)
    }

    fun sort(columns: Collection<String>, asc: Boolean = true) {
        this.sort = SelectBuilder.Sort(columns.map { Column(it, mode) }.toSet(), asc)
    }

    fun sort(tableName: String, columnName: String, asc: Boolean = true) {
        this.sort = SelectBuilder.Sort(setOf(Column(columnName, mode, tableName)), asc)
    }

    fun sort(tableColumns: Map<String, String>, asc: Boolean = true) {
        this.sort = SelectBuilder.Sort(tableColumns.entries.map { Column(it.value, mode, table = it.key) }.toSet(), asc)
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

class DSLAggregationBuilder(mode: SqlMode) : DSLAggregationFunctionsBuilder(mode) {
    fun table(tableName: String, builderActions: DSLAggregationFunctionsBuilder.() -> Unit) {
        DSLAggregationFunctionsBuilder(mode, tableName, functions, columns).builderActions()
    }

    internal fun createAggregation() = SelectBuilder.Aggregation(columns, functions)
}

open class DSLAggregationFunctionsBuilder(
    protected val mode: SqlMode,
    private val tableName: String? = null,
    protected val functions: MutableMap<String, SelectBuilder.AggregationFunction> = mutableMapOf(),
    protected val columns: MutableSet<Column> = mutableSetOf()
) {
    fun groupBy(vararg columnNames: String) {
        columnNames.forEach {
            columns.add(Column(it, mode, tableName))
        }
    }

    fun count(alias: String) = columnFunction("count", "*", alias)
    fun count(column: String, alias: String) = columnFunction("count", column, alias)
    fun max(column: String, alias: String) = columnFunction("max", column, alias)
    fun min(column: String, alias: String) = columnFunction("min", column, alias)
    fun avg(column: String, alias: String) = columnFunction("avg", column, alias)
    fun sum(column: String, alias: String) = columnFunction("sum", column, alias)

    fun function(function: String, alias: String) {
        functions[alias] = SelectBuilder.DBFunction(function)
    }

    private fun columnFunction(function: String, column: String, alias: String) {
        functions[alias] = SelectBuilder.ColumnFunction(function, Column(column, mode, tableName))
    }
}

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

class DSLSelectConditionBuilder(mode: SqlMode, private val tableName: String? = null) :
    DSLHierarchyConditionBuilder(mode, tableName) {
    fun table(tableOrAliasName: String, builderActions: DSLConditionBuilder.() -> Unit) {
        DSLConditionBuilder(mode, tableOrAliasName, andJoint).builderActions()
    }

    fun or(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(mode, tableName)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLSelectConditionBuilder.() -> Unit) {
        val builder = DSLSelectConditionBuilder(mode, tableName)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}

class DSLUpdateConditionBuilder(mode: SqlMode) : DSLHierarchyConditionBuilder(mode, null) {
    fun or(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val builder = DSLUpdateConditionBuilder(mode)
        builder.builderActions()
        orJoint.parts.add(builder.createWhereCondition())
    }

    fun and(builderActions: DSLUpdateConditionBuilder.() -> Unit) {
        val builder = DSLUpdateConditionBuilder(mode)
        builder.builderActions()
        andJoint.parts.add(builder.createWhereCondition())
    }
}

open class DSLHierarchyConditionBuilder(mode: SqlMode, tableName: String? = null) :
    DSLConditionBuilder(mode, tableName) {

    protected val orJoint: Or = Or()

    internal fun createWhereCondition(): WherePart =
        if (orJoint.parts.isEmpty()) {
            andJoint
        } else {
            orJoint.parts.add(0, andJoint)
            orJoint
        }
}

open class DSLConditionBuilder(
    protected val mode: SqlMode,
    private val tableName: String? = null,
    protected val andJoint: And = And()
) {
    fun id(value: Any) {
        column("id", WhereOps.eq, value)
    }

    fun column(columnName: String, value: Any) {
        column(columnName, WhereOps.eq, value)
    }

    fun columns(vararg pairs: Pair<String, Any?>) {
        pairs.forEach { (columnName, value) ->
            when (value) {
                null -> columnIsNull(columnName)
                is Collection<*> -> column(columnName, value as Collection<Any>)
                else -> column(columnName, value)
            }
        }
    }

    fun column(columnName: String, value: Collection<Any>) {
        column(columnName, WhereOps.`in`, value)
    }

    fun column(columnName: String, op: WhereOps, value: Any) {
        andJoint.parts.add(WhereColumn(Column(columnName, mode, tableName), op, value))
    }

    fun columnIsNull(columnName: String) {
        andJoint.parts.add(WhereColumnIsNull(Column(columnName, mode, tableName)))
    }

    fun columnNotNull(columnName: String) {
        andJoint.parts.add(WhereColumnIsNotNull(Column(columnName, mode, tableName)))
    }

    fun generate(columnName: String, actions: SqlGenerator.() -> Unit) {
        andJoint.parts.add(WhereGeneratedSql(Column(columnName, mode, tableName), actions))
    }
}