package io.hatis.utils.db.io.hatis

import io.hatis.db.DeleteBuilder
import java.lang.IllegalArgumentException

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
       val  dslConditionBuilder = DSLConditionBuilder()
        dslConditionBuilder.builderActions()
        this.dslColumnsBuilder = dslColumnsBuilder
    }

    internal fun createUpdateBuilder(tableName: String) = UpdateBuilder(
        tableName = tableName,
        columns = dslColumnsBuilder.columns,
        where = dslConditionBuilder?.createWhereCondition()
    )
}

class DSLSelectBuilder {
    private var dslConditionBuilder: DSLConditionBuilder? = null
    private var columns: Set<String> = emptySet()
    private var sort: SelectBuilder.Sort? = null
    private var limit: SelectBuilder.Limit? = null

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

    fun sort(columnName: String, asc: Boolean = true) {
        this.sort = SelectBuilder.Sort(columnName, asc)
    }

    fun limit(count: Int) {
        this.limit = this.limit?.copy(count = count) ?: SelectBuilder.Limit(count = count)
    }

    fun from(offset: Int) {
        this.limit = this.limit?.copy(offset = offset) ?: SelectBuilder.Limit(offset)
    }

    fun fromTo(from: Int, to: Int) {
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
        sort = sort
    )
}

class DSLConditionBuilder {
    private val columnConditions: MutableList<WherePart> = mutableListOf()
    private val subConditions: MutableList<DSLConditionBuilder> = mutableListOf()

    fun id(value: Any) {
        column("id", WhereOps.eq, value)
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

    internal fun isNotEmpty() = columnConditions.isNotEmpty()

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