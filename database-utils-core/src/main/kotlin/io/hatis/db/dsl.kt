package io.hatis.db

fun sqlInsert(tableName: String, mode: SqlMode = SqlMode.PG, builderActions: DSLInsertBuilder.() -> Unit): InsertBuilder {
    val dslInsertBuilder = DSLInsertBuilder(mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createInsertBuilder(tableName)
}

fun sqlUpdate(tableName: String, mode: SqlMode = SqlMode.PG, builderActions: DSLUpdateBuilder.() -> Unit): UpdateBuilder {
    val dslInsertBuilder = DSLUpdateBuilder(mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createUpdateBuilder(tableName)
}

fun sqlSelect(tableName: String, mode: SqlMode = SqlMode.PG, builderActions: DSLSelectBuilder.() -> Unit): SelectBuilder {
    val dslInsertBuilder = DSLSelectBuilder(mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createSelectBuilder(tableName)
}

fun sqlDelete(tableName: String, mode: SqlMode = SqlMode.PG, builderActions: DSLDeleteBuilder.() -> Unit): DeleteBuilder {
    val dslInsertBuilder = DSLDeleteBuilder(mode)
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createDeleteBuilder(tableName)
}

class DSLDeleteBuilder(private val mode: SqlMode) {
    private lateinit var dslConditionBuilder: DSLConditionBuilder

    fun where(builderActions: DSLConditionBuilder.() -> Unit) {
        dslConditionBuilder = DSLConditionBuilder(mode)
        dslConditionBuilder.builderActions()
    }

    internal fun createDeleteBuilder(tableName: String) = DeleteBuilder(
        tableName = tableName,
        where = dslConditionBuilder.createWhereCondition(),
        mode = mode
    )
}

class DSLColumnsBuilder(private val mode: SqlMode) {
    internal val columns: MutableMap<Column, Any?> = mutableMapOf()

    fun columns(params: Map<String, Any?>) {
        columns.putAll(params.mapKeys { (columnName, _) -> Column(columnName, mode) })
    }

    fun column(columnName: String, value: Any?) {
        columns[Column(columnName, mode)] = value
    }
}

class DSLInsertBuilder(private val mode: SqlMode) {
    private var values: MutableList<Map<Column, Any?>> = mutableListOf()
    private var genKeys: Set<String> = emptySet()

    fun values(builderActions: DSLColumnsBuilder.() -> Unit) {
        val dslColumnsBuilder = DSLColumnsBuilder(mode)
        dslColumnsBuilder.builderActions()
        values.add(dslColumnsBuilder.columns)
    }

    fun generatedKeys(vararg keys: String) {
        genKeys = keys.toSet()
    }

    internal fun createInsertBuilder(tableName: String) = InsertBuilder(
        tableName = tableName,
        values = values,
        generatedKeys = genKeys,
        mode = mode
    )
}

class DSLUpdateBuilder(private val mode: SqlMode) {
    private lateinit var dslColumnsBuilder: DSLColumnsBuilder
    private var dslConditionBuilder: DSLConditionBuilder? = null

    fun set(builderActions: DSLColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLColumnsBuilder(mode)
        dslColumnsBuilder.builderActions()
    }

    fun where(builderActions: DSLConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLConditionBuilder(mode)
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

class DSLSelectBuilder(private val mode: SqlMode) {
    private var dslConditionBuilder: DSLConditionBuilder? = null
    private var dslAggregationBuilder: DSLAggregationBuilder? = null
    private var columns: Collection<Column> = emptySet()
    private var sort: SelectBuilder.Sort? = null
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false

    fun distinct() {
        distinct = true
    }

    fun returns(columns: Set<String>) {
        this.columns = columns.map { Column(it, mode) }
    }

    fun returns(vararg columns: String) {
        this.columns = columns.toSet().map { Column(it, mode) }
    }

    fun where(builderActions: DSLConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLConditionBuilder(mode)
        dslConditionBuilder.builderActions()

        this.dslConditionBuilder = dslConditionBuilder
    }

    fun aggregation(vararg groupBy: String, builderActions: DSLAggregationBuilder.() -> Unit) {
        val dslAggregationBuilder = DSLAggregationBuilder(groupBy.toSet(), mode)
        dslAggregationBuilder.builderActions()
        this.dslAggregationBuilder = dslAggregationBuilder
    }

    fun aggregation(builderActions: DSLAggregationBuilder.() -> Unit) {
        val dslAggregationBuilder = DSLAggregationBuilder(emptySet(), mode)
        dslAggregationBuilder.builderActions()
        this.dslAggregationBuilder = dslAggregationBuilder
    }

    fun sort(columnName: String, asc: Boolean = true) {
        this.sort = SelectBuilder.Sort(columnName, asc)
    }

    fun limit(count: Int) {
        this.limit = this.limit?.copy(count = count) ?: SelectBuilder.Limit(count = count)
    }

    fun offset(offset: Int) {
        this.limit = this.limit?.copy(offset = offset) ?: SelectBuilder.Limit(offset)
    }

    fun range(from: Int, to: Int) {
        if(to < from) {
            throw IllegalArgumentException("to < offset")
        }
        this.limit = SelectBuilder.Limit(from, to - from)
    }

    internal fun createSelectBuilder(tableName: String) = SelectBuilder(
        tableName = tableName,
        columns = columns,
        where = dslConditionBuilder?.createWhereCondition(),
        limit = limit,
        sort = sort,
        distinct = distinct,
        aggregation = dslAggregationBuilder?.createAggregation(),
        mode = mode
    )
}

class DSLAggregationBuilder(
    private val columns: Set<String>,
    private val mode: SqlMode
) {
    private val functions: MutableMap<String, SelectBuilder.AggregationFunction> = mutableMapOf()

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
        functions[alias] = SelectBuilder.ColumnFunction(function, Column(column, mode))
    }

    internal fun createAggregation() = SelectBuilder.Aggregation(columns, functions)
}

class DSLConditionBuilder(private val mode: SqlMode) {
    private val columnConditions: MutableList<WherePart> = mutableListOf()
    private val subConditions: MutableList<DSLConditionBuilder> = mutableListOf()

    fun id(value: Any) {
        column("id", WhereOps.eq, value)
    }

    fun column(columnName: String, value: Any) {
        columnConditions.add(WhereColumn(Column(columnName, mode), WhereOps.eq, value))
    }

    fun column(columnName: String, value: Collection<Any>) {
        columnConditions.add(WhereColumn(Column(columnName, mode), WhereOps.`in`, value))
    }

    fun column(columnName: String, op: WhereOps, value: Any) {
        columnConditions.add(WhereColumn(Column(columnName, mode), op, value))
    }

    fun columnIsNull(columnName: String) {
        columnConditions.add(WhereColumnIsNull(Column(columnName, mode)))
    }

    fun columnNotNull(columnName: String) {
        columnConditions.add(WhereColumnIsNotNull(Column(columnName, mode)))
    }

    fun or(builderActions: DSLConditionBuilder.() -> Unit) {
        val builder = DSLConditionBuilder(mode)
        builder.builderActions()
        subConditions.add(builder)
    }

    private fun isNotEmpty() = columnConditions.isNotEmpty()

    internal fun createWhereCondition(): WherePart {
        val conditions = mutableListOf(this)
            .plus(subConditions)
            .filter { it.isNotEmpty() }

        if(conditions.size == 1)
            return generateWhere(conditions.first())

        return Or(conditions.map { generateWhere(it) })
    }

    private fun generateWhere(it: DSLConditionBuilder) = when (it) {
        this -> And(it.columnConditions)
        else -> it.createWhereCondition()
    }
}