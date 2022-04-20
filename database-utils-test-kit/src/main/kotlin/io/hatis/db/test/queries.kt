package io.hatis.db.test

import io.hatis.db.SqlMode
import io.hatis.db.WhereOps
import io.hatis.db.sqlSelect
import java.time.Instant

fun selectAllUsers(mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode)
fun countUsers(mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    aggregation { count("count") }
}

fun selectByUserId(userId: Long, mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    where { id(userId) }
}

fun selectByUserIds(userId: Collection<Long>, mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    where { column("id", userId) }
}

fun selectUsersWithOffset(offset: Int, limit: Int, mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    offset(offset)
    limit(limit)
}

fun selectUsersInRange(from: Int, to: Int, mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    range(from, to)
}

fun selectUserAgeBetween(from: Int, to: Int, mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    where {
        column("age", WhereOps.gte, from)
        column("age", WhereOps.lte, to)
    }
}

fun selectUserWithNameLike(name: String, mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    where {
        column("name", WhereOps.like, name)
    }
}

fun selectDeletedUsers(mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    where {
        columnNotNull("deleted")
    }
}

fun selectUsersNameAndAge(mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    returns("name", "age")
}

fun selectOrderFrom(time: Instant, mode: SqlMode = SqlMode.PG) = sqlSelect("orders", mode) {
    where {
        column("created", WhereOps.gt, time)
    }
}

fun selectOrderTo(time: Instant, mode: SqlMode = SqlMode.PG) = sqlSelect("orders", mode) {
    where {
        column("created", WhereOps.lt, time)
    }
}

fun selectUserOrders(userId: Long, mode: SqlMode = SqlMode.PG) = sqlSelect("orders", mode) {
    returns("id" to "orderId")
    returnsFrom(
        "users",
        "name" to "name",
        "id" to "userId"
    )
    join("users", "id") { on("user_id") }
    where { id(userId) }
}

fun selectUsersOrdersCount(mode: SqlMode = SqlMode.PG) = sqlSelect("users", mode) {
    returns("name")
    join("orders", "user_id") { on("id") }
    aggregation {
        groupBy("id")
        table("orders") { count("id") }
    }
}