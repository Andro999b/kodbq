package io.hatis.kodbq.test

import io.hatis.kodbq.*
import java.time.OffsetDateTime

object Users : Table("users") {
    val id = column("id")
    val name = column("name")
    val age = column("age")
    val created = column("created")
    val deleted = column("deleted")
}

object Orders : Table("orders") {
    val id = column("id")
    val created = column("created")
    val userId = column("user_id")
    val price = column("price")
    val article = column("article")
    val userRef = userId refernce Users.id
}

object OrderStatus : Table("order_status") {
    val orderId = column("order_id")
    val status = column("status")
    val ordersRef = orderId refernce Orders.id
}

fun selectAllUsers() = sqlSelect(Users)
fun countUsers() = sqlSelect(Users) {
    returns { count("count") }
}

fun selectUsersNames() = sqlSelect(Users) {
    distinct()
    returns { column(Users.name) }
}

fun selectByUserId(userId: Long) = sqlSelect(Users) {
    where { column(Users.id, userId) }
}

fun selectByUserIds(userId: List<Long>) = sqlSelect(Users) {
    where { columns(Users.id to userId) }
}

fun selectUsersWithOffset(offset: Int, limit: Int) = sqlSelect(Users) {
    sort { asc(Users.id) }
    offset(offset)
    limit(limit)
}

fun selectUsersInRange(from: Int, to: Int) = sqlSelect(Users) {
    sort { asc(Users.id) }
    range(from, to)
}

fun selectUserAgeBetween(from: Int, to: Int) = sqlSelect(Users) {
    where {
        column(Users.age) gte from
        and {
            column(Users.age) lte to
        }
    }
}

fun selectUserWithNameLike(name: String) = sqlSelect(Users) {
    where {
        column(Users.name) like name
    }
}

fun selectDeletedUsers() = sqlSelect(Users) {
    where {
        columnNotNull(Users.deleted)
    }
}

fun selectUsersNameAndAge() = sqlSelect(Users) {
    returns {
        columns(Users.name)
        columns(Users.age to "userAge")
    }
}

fun selectOrderSince(time: OffsetDateTime) = sqlSelect(Orders) {
    where {
        column(Orders.created) gt time
    }
}

fun selectOrderUntil(time: OffsetDateTime) = sqlSelect(Orders) {
    where {
        column(Orders.created) lt time
    }
}

fun selectOrderById(id: Long) = sqlSelect(Orders) { where { column(Orders.id, id) } }

fun selectUserOrders(userId: Long) = sqlSelect(Orders) {
    returns {
        columns(Orders.id to "orderid")
        column(Users.name)
        column(Users.id, "userid")
    }
    join(Users.id) on Orders.userId
    where { column(Orders.userId) eq userId }
}

fun selectUsersWithOrderMinPrice(minPrice: Int) = sqlSelect(Orders) {
    groupBy(Orders.userId)
    having {
        max(Orders.price) gte minPrice
    }
}

fun selectUsersOrdersFrom(from: OffsetDateTime) = sqlSelect(Orders) {
    returns {
        columns(Orders.id to "orderid")
        columns(setOf(Orders.article, Orders.price, Users.name))
        column(Users.id, "userid")
        column(OrderStatus.status, "order_status")
    }
    join(Users.id) on Orders.userId
    join(OrderStatus.ordersRef)
    where {
        column(Orders.created) gte from
        column(Users.deleted).isNull()
    }
}

fun selectUsersOrdersCount() = sqlSelect(Users) {
    returns {
        column(Users.name)
        count("count")
    }
    rightJoin(Orders.userRef)
    groupBy(Users.id, Users.name)
    sort { asc(Users.id) }
}

fun selectUsersAvgOrderPrice() = sqlSelect(Users) {
    returns {
        column(Users.name)
        avg(Orders.price, "avg_price")
    }
    rightJoin(Orders.userRef)
    groupBy(Users.id, Users.name)
    sort { asc(Users.id) }
}

fun selectUsersSortedByAgeAndCreated() = sqlSelect(Users) {
    sort {
        desc(Users.age)
        asc(Users.created)
    }
}

fun selectOrdersByCreatedAndPrice() = sqlSelect(Orders) {
    sort { asc(Orders.created, Orders.price) }
}

fun selectUsersWithAgeInRanges(ranges: Collection<IntRange>) = sqlSelect(Users) {
    where {
        ranges.forEach { range ->
            or {
                column(Users.age) gte range.first
                column(Users.age) lte range.last
            }
        }
    }
}

fun selectUserIdsOfLaptopAndChargerOrders() = sqlSelect(Orders) {
    where { column(Orders.article, "laptop") }
    union(all = true) {
        where { column(Orders.article, "charger") }
    }
}

fun insertOrders(orders: Collection<TestOrder>, returnIds: Boolean = true) = sqlInsert(Orders) {
    orders.forEach {
        values {
            column(Orders.userId, it.userId)
            columns(
                mapOf(
                    Orders.article to it.article,
                    Orders.price to it.price
                )
            )
            native(Orders.created) { if (dialect == SqlDialect.MS_SQL) "getdate()" else "now()" }
        }
    }
    if (returnIds) generatedKeys("id")
}

fun updateOrder(orderId: Long, article: String, price: Double) = sqlUpdate(Orders) {
    set {
        columns(
            Orders.article to article,
            Orders.price to price
        )
    }
    where { column(Orders.id, orderId) }
}

fun deleteOrder(orderId: Long) = sqlDelete(Orders) {
    where { column(Orders.id, orderId) }
}

fun deleteOrdersWithDateRanges(ranges: Collection<Pair<OffsetDateTime, OffsetDateTime>>) = sqlDelete(Orders) {
    where {
        ranges.forEach { (first, second) ->
            or {
                column(Orders.created) gt first
                and {
                    column(Orders.created) lt second
                }
            }
        }
    }
}