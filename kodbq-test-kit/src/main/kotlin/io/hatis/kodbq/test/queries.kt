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
    where { Users.id eq userId }
}

fun selectByUserIds(userId: List<Long>) = sqlSelect(Users) {
    where { Users.id `in` userId }
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
        Users.age gte from
        and {
            Users.age lte to
        }
    }
}

fun selectUserWithNameLike(name: String) = sqlSelect(Users) {
    where {
        Users.name like name
    }
}

fun selectDeletedUsers() = sqlSelect(Users) {
    where {
        Users.deleted.notNull()
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
        Orders.created gt time
    }
}

fun selectOrderUntil(time: OffsetDateTime) = sqlSelect(Orders) {
    where {
        Orders.created lt time
    }
}

fun selectOrderById(id: Long) = sqlSelect(Orders) { where { Orders.id eq id } }

fun selectUserOrders(userId: Long) = sqlSelect(Orders) {
    returns {
        columns(Orders.id to "orderid")
        column(Users.name)
        column(Users.id, "userid")
    }
    Users.id joinOn Orders.userId
    where { Orders.userId eq userId }
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
    Users.id joinOn Orders.userId
    join(OrderStatus.ordersRef)
    where {
        Orders.created gte from
        Users.deleted.isNull()
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
                Users.age gte range.first
                Users.age lte range.last
            }
        }
    }
}

fun selectUserIdsOfLaptopAndChargerOrders() = sqlSelect(Orders) {
    where { Orders.article eq "laptop" }
    union(all = true) {
        where { Orders.article eq "charger" }
    }
}

fun insertOrders(orders: Collection<TestOrder>, returnIds: Boolean = true) = sqlInsert(Orders) {
    orders.forEach {
        values {
            Orders.userId to it.userId
            Orders.article to it.article
            Orders.price to it.price
            native(Orders.created) { if (dialect == SqlDialect.MS_SQL) "getdate()" else "now()" }
        }
    }
    if (returnIds) generatedKeys(Orders.id)
}

fun updateOrder(orderId: Long, article: String, price: Double) = sqlUpdate(Orders) {
    set {
        Orders.article to article
        Orders.price to price
    }
    where { Orders.id eq orderId }
}

fun deleteOrder(orderId: Long) = sqlDelete(Orders) {
    where { Orders.id eq orderId }
}

fun deleteOrdersWithDateRanges(ranges: Collection<Pair<OffsetDateTime, OffsetDateTime>>) = sqlDelete(Orders) {
    where {
        ranges.forEach { (first, second) ->
            or {
                Orders.created gt first
                and {
                    Orders.created lt second
                }
            }
        }
    }
}