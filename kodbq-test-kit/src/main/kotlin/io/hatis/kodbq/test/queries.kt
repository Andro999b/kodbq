package io.hatis.kodbq.test

import io.hatis.kodbq.WhereOps
import io.hatis.kodbq.sqlSelect
import java.time.Instant

fun selectAllUsers() = sqlSelect("users")
fun countUsers() = sqlSelect("users") {
    returns { count("count") }
}

fun selectByUserId(userId: Long) = sqlSelect("users") {
    where { id(userId) }
}

fun selectByUserIds(userId: Collection<Long>) = sqlSelect("users") {
    where { columns("id" to userId) }
}

fun selectUsersWithOffset(offset: Int, limit: Int) = sqlSelect("users") {
    offset(offset)
    limit(limit)
}

fun selectUsersInRange(from: Int, to: Int) = sqlSelect("users") {
    range(from, to)
}

fun selectUserAgeBetween(from: Int, to: Int) = sqlSelect("users") {
    where {
        column("age", WhereOps.GTE, from)
        column("age", WhereOps.LTE, to)
    }
}

fun selectUserWithNameLike(name: String) = sqlSelect("users") {
    where {
        column("name", WhereOps.LIKE, name)
    }
}

fun selectDeletedUsers() = sqlSelect("users") {
    where {
        columnNotNull("deleted")
    }
}

fun selectUsersNameAndAge() = sqlSelect("users") {
    returns {
        columns("name")
        columns("age" to "userAge")
    }
}

fun selectOrderFrom(time: Instant) = sqlSelect("orders") {
    where {
        column("created", WhereOps.GT, time)
    }
}

fun selectOrderTo(time: Instant) = sqlSelect("orders") {
    where {
        column("created", WhereOps.LT, time)
    }
}

fun selectUserOrders(userId: Long) = sqlSelect("orders") {
    returns {
        columns("id" to "orderId")
        table("users") {
            columns("name")
            columns("id" to "userId")
        }
    }
    join("users", "id") { on("user_id") }
    where { id(userId) }
}

fun selectUsersOrdersCount() = sqlSelect("users") {
    returns {
        columns("name")
        table("orders") { count("count") }
    }
    join("orders", "user_id") { on("id") }
    groupBy("id")
}

fun selectUsersSorted() = sqlSelect("users") {
    sort("age", asc = false)
    sort("created")
}

fun selectUsersWithAgeInRanges(ranges: Collection<IntRange>) = sqlSelect("users") {
    where {
        ranges.forEach { range ->
            or {
                column("age", WhereOps.GTE, range.first)
                column("age", WhereOps.LTE, range.last)
            }
        }
    }
}