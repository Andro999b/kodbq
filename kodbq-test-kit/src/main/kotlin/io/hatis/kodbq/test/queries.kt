package io.hatis.kodbq.test

import io.hatis.kodbq.*
import java.time.OffsetDateTime

fun selectAllUsers() = sqlSelect("users")
fun countUsers() = sqlSelect("users") {
    returns { count("count") }
}

fun selectUsersNames() = sqlSelect("users") {
    distinct()
    returns { column("name") }
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
        and {
            column("age", WhereOps.LTE, to)
        }
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

fun selectOrderSince(time: OffsetDateTime) = sqlSelect("orders") {
    where {
        column("created", WhereOps.GT, time)
    }
}

fun selectOrderUntil(time: OffsetDateTime) = sqlSelect("orders") {
    where {
        column("created", WhereOps.LT, time)
    }
}

fun selectUserOrders(userId: Long) = sqlSelect("orders") {
    returns {
        columns("id" to "orderId")
        table("users") {
            column("name")
            column("id", "userId")
        }
    }
    join("users", "id") on "user_id"
    where { column("user_id") eq userId }
}

fun selectUsersOrdersFrom(from: OffsetDateTime) = sqlSelect("orders") {
    returns {
        columns("id" to "orderId")
        columns(setOf("article", "price"))
        table("users") {
            column("name")
            column("id", "userId")
        }
        table("order_status") {
            column("status", "order_status")
        }
    }
    join("users", "id") on "user_id"
    join("order_status", "order_id").on("orders", "id")
    where {
        column("created") eq from
        table("users") { column("deleted").notNull() }
    }
}

fun selectUsersOrdersCount() = sqlSelect("users") {
    returns {
        column("name")
        table("orders") { count("count") }
    }
    join("orders", "user_id") on "id"
    groupBy("id")
}

fun selectUsersSortedByAgeAndCreated() = sqlSelect("users") {
    sort("age", asc = false)
    sort("created")
}

fun selectOrdersByCteatedAndPrice() = sqlSelect("orders") {
    sort(setOf("created", "price"))
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

fun insertOrders(orders: Collection<TestOrder>) = sqlInsert("orders") {
    orders.forEach {
        values {
            column("user_id", it.userId)
            columns(mapOf(
                "article" to it.article,
                "price" to it.price
            ))
            native("created") { "now()" }
        }
    }
    generatedKeys("id")
}

fun updateOrder(orderId: Long, article: String, price: Double) = sqlUpdate("orders") {
    set {
        columns(
            "article" to article,
            "price" to price
        )
    }
    where { id(orderId) }
}

fun deleteOrder(orderId: Long) = sqlDelete("orders") {
    where { id(orderId) }
}

fun deleteOrdersWithDateRanges(ranges: Collection<Pair<OffsetDateTime, OffsetDateTime>>) = sqlDelete("orders") {
    where {
        ranges.forEach {(first, second) ->
            or {
                column("created") gt first
                and {
                    column("created") lt second
                }
            }
        }
    }
}