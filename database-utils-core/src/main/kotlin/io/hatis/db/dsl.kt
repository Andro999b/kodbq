package io.hatis.db

fun sqlInsert(tableName: String, builderActions: DSLInsertBuilder.() -> Unit): InsertBuilder {
    val dslInsertBuilder = DSLInsertBuilder()
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createInsertBuilder(tableName)
}

fun sqlUpdate(tableName: String, builderActions: DSLUpdateBuilder.() -> Unit): UpdateBuilder {
    val dslInsertBuilder = DSLUpdateBuilder()
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createUpdateBuilder(tableName)
}

fun sqlSelect(tableName: String, builderActions: DSLSelectBuilder.() -> Unit): SelectBuilder {
    val dslInsertBuilder = DSLSelectBuilder()
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createSelectBuilder(tableName)
}

fun sqlDelete(tableName: String, builderActions: DSLDeleteBuilder.() -> Unit): DeleteBuilder {
    val dslInsertBuilder = DSLDeleteBuilder()
    dslInsertBuilder.builderActions()
    return dslInsertBuilder.createDeleteBuilder(tableName)
}

class DSLDeleteBuilder {
    private lateinit var dslConditionBuilder: DSLConditionBuilder

    fun where(builderActions: DSLConditionBuilder.() -> Unit) {
        dslConditionBuilder = DSLConditionBuilder()
        dslConditionBuilder.builderActions()
    }

    internal fun createDeleteBuilder(tableName: String) = DeleteBuilder(
        tableName = tableName,
        where = dslConditionBuilder.createWhereCondition()
    )
}

class DSLColumnsBuilder {
    internal val columns: MutableMap<String, Any?> = mutableMapOf()

    fun columns(params: Map<String, Any?>) {
        columns.putAll(params)
    }

    fun column(columnName: String, value: Any?) {
        columns.put(columnName, value)
    }
}

class DSLInsertBuilder {
    private lateinit var dslColumnsBuilder: DSLColumnsBuilder
    private var genKeys: Set<String> = emptySet()

    fun values(builderActions: DSLColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLColumnsBuilder()
        dslColumnsBuilder.builderActions()
    }

    fun generatedKeys(vararg keys: String) {
        genKeys = keys.toSet()
    }

    internal fun createInsertBuilder(tableName: String) = InsertBuilder(
        tableName = tableName,
        columns = dslColumnsBuilder.columns,
        generatedKeys = genKeys
    )
}

class DSLUpdateBuilder {
    private lateinit var dslColumnsBuilder: DSLColumnsBuilder
    private var dslConditionBuilder: DSLConditionBuilder? = null

    fun set(builderActions: DSLColumnsBuilder.() -> Unit) {
        dslColumnsBuilder = DSLColumnsBuilder()
        dslColumnsBuilder.builderActions()
    }

    fun where(builderActions: DSLConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLConditionBuilder()
        dslConditionBuilder.builderActions()
        this.dslConditionBuilder = dslConditionBuilder
    }

    internal fun createUpdateBuilder(tableName: String) = UpdateBuilder(
        tableName = tableName,
        columns = dslColumnsBuilder.columns,
        where = dslConditionBuilder?.createWhereCondition()
    )
}

class DSLSelectBuilder {
    private var dslConditionBuilder: DSLConditionBuilder? = null
    private var dslAggregationBuilder: DSLAggregationBuilder? = null
    private var columns: Set<String> = emptySet()
    private var sort: SelectBuilder.Sort? = null
    private var limit: SelectBuilder.Limit? = null
    private var distinct: Boolean = false

    fun columns(columns: Set<String>) {
        this.columns = columns
    }

    fun columns(vararg columns: String) {
        this.columns = columns.toSet()
    }

    fun where(builderActions: DSLConditionBuilder.() -> Unit) {
        val dslConditionBuilder = DSLConditionBuilder()
        dslConditionBuilder.builderActions()

        this.dslConditionBuilder = dslConditionBuilder
    }

    fun aggregation(vararg groupBy: String, builderActions: DSLAggregationBuilder.() -> Unit) {
        val dslAggregationBuilder = DSLAggregationBuilder(groupBy.toSet())
        dslAggregationBuilder.builderActions()
        this.dslAggregationBuilder = dslAggregationBuilder
    }

    fun aggregation(builderActions: DSLAggregationBuilder.() -> Unit) {
        val dslAggregationBuilder = DSLAggregationBuilder(emptySet())
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
        aggregation = dslAggregationBuilder?.createAggregation()
    )
}

class DSLAggregationBuilder(
    private val columns: Set<String>
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
        functions[alias] = SelectBuilder.ColumnFunction(function, column)
    }

    internal fun createAggregation() = SelectBuilder.Aggregation(columns, functions)
}

class DSLConditionBuilder {
    private val columnConditions: MutableList<WherePart> = mutableListOf()
    private val subConditions: MutableList<DSLConditionBuilder> = mutableListOf()

    fun id(value: Any) {
        column("id", WhereOps.eq, value)
    }

    fun column(columnName: String, value: Any) {
        columnConditions.add(Column(columnName, WhereOps.eq, value))
    }

    fun column(columnName: String, value: Collection<Any>) {
        columnConditions.add(Column(columnName, WhereOps.`in`, value))
    }

    fun column(columnName: String, op: WhereOps, value: Any) {
        columnConditions.add(Column(columnName, op, value))
    }

    fun columnIsNull(columnName: String) {
        columnConditions.add(ColumnIsNull(columnName))
    }

    fun columnNotNull(columnName: String) {
        columnConditions.add(ColumnIsNotNull(columnName))
    }

    fun or(builderActions: DSLConditionBuilder.() -> Unit) {
        val builder = DSLConditionBuilder()
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