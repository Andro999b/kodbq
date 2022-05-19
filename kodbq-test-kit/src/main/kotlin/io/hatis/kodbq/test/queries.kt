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

fun selectByUserIds(userId: List<Long>) = sqlSelect("users") {
    where { columns("id" to userId) }
}

fun selectUsersWithOffset(offset: Int, limit: Int) = sqlSelect("users") {
    sort("id")
    offset(offset)
    limit(limit)
}

fun selectUsersInRange(from: Int, to: Int) = sqlSelect("users") {
    sort("id")
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

fun selectOrderById(id: Long) = sqlSelect("orders") { where { id(id) } }

fun selectUserOrders(userId: Long) = sqlSelect("orders") {
    returns {
        columns("id" to "orderid")
        table("users") {
            column("name")
            column("id", "userid")
        }
    }
    join("users", "id") on "user_id"
    where { column("user_id") eq userId }
}

fun selectUsersOrdersFrom(from: OffsetDateTime) = sqlSelect("orders") {
    returns {
        columns("id" to "orderid")
        columns(setOf("article", "price"))
        table("users") {
            column("name")
            column("id", "userid")
        }
        table("order_status") {
            column("status", "order_status")
        }
    }
    join("users", "id") on "user_id"
    join("order_status", "order_id").on("orders", "id")
    where {
        column("created") gte from
        table("users") { column("deleted").isNull() }
    }
}

fun selectUsersOrdersCount() = sqlSelect("users") {
    returns {
        column("name")
        table("orders") { count("count") }
    }
    rightJoin("orders", "user_id") on "id"
    groupBy("id", "name")
    sort("id")
}

fun selectUsersAvgOrderPrice() = sqlSelect("users") {
    returns {
        column("name")
        table("orders") { avg("price", "avg_price")}
    }
    rightJoin("orders", "user_id") on "id"
    groupBy("id","name")
    sort("id")
}

fun selectUsersSortedByAgeAndCreated() = sqlSelect("users") {
    sort("age", asc = false)
    sort("created")
}

fun selectOrdersByCteatedAndPrice() = sqlSelect("orders") {
    sort("created", "price")
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

fun insertOrders(orders: Collection<TestOrder>, returnIds: Boolean = true) = sqlInsert("orders") {
    orders.forEach {
        values {
            column("user_id", it.userId)
            columns(mapOf(
                "article" to it.article,
                "price" to it.price
            ))
            native("created") { if(dialect == SqlDialect.MS_SQL) "getdate()" else "now()" }
        }
    }
    if(returnIds) generatedKeys("id")
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