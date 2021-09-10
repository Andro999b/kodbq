package io.hatis.db.quarkus

import io.hatis.db.*
import org.codejargon.fluentjdbc.api.FluentJdbc
import org.codejargon.fluentjdbc.api.mapper.Mappers
import java.sql.ResultSet
import java.util.*

abstract class CrudRepository<T> {
    protected abstract val mapper: (rs: ResultSet) -> T
    protected abstract val fluentJdbc: FluentJdbc
    protected abstract val tableName: String

    protected open fun insert(actions: DSLMutationColumnsBuilder.() -> Unit) {
        sqlInsert(tableName) {
            values { actions() }
        }
            .execute(fluentJdbc)
    }

    protected open fun insertAll(values: Collection<T>, actions: DSLMutationColumnsBuilder.(T) -> Unit) {
        if(values.isNotEmpty()) {
            sqlInsert(tableName) {
                values.forEach { values { actions(it) } }
            }
        }
    }

    protected open fun insertAllAndGetIds(values: Collection<T>, actions: DSLMutationColumnsBuilder.(T) -> Unit) {
        if(values.isNotEmpty()) {
            sqlInsert(tableName) {
                values.forEach { values { actions(it) } }
                generatedKeys("id")
            }
        }
    }

    protected open fun insertAndGetId(actions: DSLMutationColumnsBuilder.() -> Unit): Long =
        sqlInsert(tableName) {
            values { actions() }
            generatedKeys("id")
        }
            .build(fluentJdbc)
            .runFetchGenKeys(Mappers.singleLong())
            .first().generatedKeys()
            .first()

    protected open fun update(actions: DSLUpdateBuilder.() -> Unit) {
        sqlUpdate(tableName) { actions() }
            .execute(fluentJdbc)
    }

    protected open fun upsert(id: Long, actions: DSLMutationColumnsBuilder.() -> Unit) =
        if (exist { id(id) }) {
            update {
                where { id(id) }
                set { actions() }
            }
        } else {
            insert {
                column("id", id)
                actions()
            }
        }

    protected open fun upsert(cols: Map<String, Any>, actions: DSLMutationColumnsBuilder.() -> Unit) =
        if (exist { columns(cols) }) {
            update {
                where { columns(cols)  }
                set { actions() }
            }
        } else {
            insert {
                columns(cols)
                actions()
            }
        }

    protected open fun delete(actions: DSLUpdateConditionBuilder.() -> Unit) {
        sqlDelete(tableName) { where { actions() } }
            .execute(fluentJdbc)
    }

    protected open fun exist(actions: DSLSelectConditionBuilder.() -> Unit): Boolean = count(actions) > 0

    protected open fun count(actions: DSLSelectConditionBuilder.() -> Unit): Int =
        sqlSelect(tableName) {
            aggregation { count("count") }
            where { actions() }
        }
            .build(fluentJdbc)
            .singleResult(Mappers.singleInteger())

    protected open fun select(actions: DSLSelectBuilder.() -> Unit): Collection<T> =
        sqlSelect(tableName) { actions() }
            .build(fluentJdbc)
            .listResult(mapper)

    protected open fun selectOne(actions: DSLSelectBuilder.() -> Unit): Optional<T> =
        sqlSelect(tableName) {
            actions()
            limit(1)
        }
            .build(fluentJdbc)
            .firstResult(mapper)

    protected open fun selectWhere(actions: DSLSelectConditionBuilder.() -> Unit): Collection<T> =
        sqlSelect(tableName) { where { actions() } }
            .build(fluentJdbc)
            .listResult(mapper)

    protected open fun selectOneWhere(actions: DSLSelectConditionBuilder.() -> Unit): Optional<T> =
        sqlSelect(tableName) {
            where { actions() }
            limit(1)
        }
            .build(fluentJdbc)
            .firstResult(mapper)

    protected open fun selectById(id: Long): Optional<T> =
        sqlSelect(tableName) { where { id(id) } }
            .build(fluentJdbc)
            .firstResult(mapper)

    protected open fun selectById(ids: Collection<Long>): Collection<T> =
        if (ids.isEmpty())
            emptyList()
        else
            sqlSelect(tableName) { where { column("id", ids) } }
                .build(fluentJdbc)
                .listResult(mapper)
}