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

    open fun insert(actions: DSLMutationColumnsBuilder.() -> Unit) {
        sqlInsert(tableName) {
            values { actions() }
        }
            .execute(fluentJdbc)
    }

    open fun insertAll(values: Collection<T>, actions: DSLMutationColumnsBuilder.(T) -> Unit) {
        sqlInsert(tableName) {
            values.forEach {
                values { actions(it) }
            }
            generatedKeys("id")
        }
    }

    open fun insertAndGetId(actions: DSLMutationColumnsBuilder.() -> Unit): Long =
        sqlInsert(tableName) {
            values { actions() }
            generatedKeys("id")
        }
            .build(fluentJdbc)
            .runFetchGenKeys(Mappers.singleLong())
            .first().generatedKeys()
            .first()

    open fun update(actions: DSLUpdateBuilder.() -> Unit) {
        sqlUpdate(tableName) { actions() }
            .execute(fluentJdbc)
    }

    open fun delete(actions: DSLUpdateConditionBuilder.() -> Unit) {
        sqlDelete(tableName) { where { actions() } }
            .execute(fluentJdbc)
    }

    open fun exist(actions: DSLSelectConditionBuilder.() -> Unit): Boolean =
        sqlSelect(tableName) {
            aggregation { count("count") }
            where { actions() }
        }
            .build(fluentJdbc)
            .singleResult(Mappers.singleLong()) > 0

    open fun select(actions: DSLSelectConditionBuilder.() -> Unit): Collection<T> =
        sqlSelect(tableName) { where { actions() } }
            .build(fluentJdbc)
            .listResult(mapper)

    open fun selectOne(actions: DSLSelectConditionBuilder.() -> Unit): Optional<T> =
        sqlSelect(tableName) { where { actions() } }
            .build(fluentJdbc)
            .firstResult(mapper)

    open fun selectById(id: Long): Optional<T> =
        sqlSelect(tableName) { where { id(id) } }
            .build(fluentJdbc)
            .firstResult(mapper)
}