import io.hatis.kodbq.SqlDialect
import io.hatis.kodbq.kodbqDialect
import io.hatis.kodbq.test.*
import io.kotest.core.spec.style.StringSpec
import java.time.Instant

class TestPGQueries : StringSpec({
    beforeTest { kodbqDialect = SqlDialect.PG }
    "select all" {
        selectAllUsers()
            .expectSqlAndParams("select * from \"users\"")
    }
    "count users" {
        countUsers()
            .expectSqlAndParams("select count(\"users\".*) as count from \"users\"")
    }
    "select user with id" {
        val userId = 1L
        selectByUserId(userId)
            .expectSqlAndParams("select * from \"users\" where \"users\".\"id\" = ?", listOf(userId))
    }
    "select user with id collection" {
        val userIds: List<Long> = listOf(1, 2, 3, 4)
        selectByUserIds(userIds)
            .expectSqlAndParams("select * from \"users\" where \"users\".\"id\" = any(?)", listOf(userIds))
    }
    "select with offset" {
        selectUsersWithOffset(100, 10)
            .expectSqlAndParams("select * from \"users\" offset 100 limit 10")
    }
    "select with range" {
        selectUsersInRange(100, 110)
            .expectSqlAndParams("select * from \"users\" offset 100 limit 10")
    }
    "select users age between" {
        selectUserAgeBetween(18, 25)
            .expectSqlAndParams(
                "select * from \"users\" where \"users\".\"age\" >= ? and \"users\".\"age\" <= ?",
                listOf(18, 25)
            )
    }
    "select users with name like" {
        selectUserWithNameLike("mike")
            .expectSqlAndParams(
                "select * from \"users\" where \"users\".\"name\" like ?",
                listOf("mike")
            )
    }
    "select deleted users" {
        selectDeletedUsers()
            .expectSqlAndParams("select * from \"users\" where \"users\".\"deleted\" is not null")
    }
    "select users age and name" {
        selectUsersNameAndAge()
            .expectSqlAndParams("select \"users\".\"name\",\"users\".\"age\" as userAge from \"users\"")
    }
    "select orders from now" {
        val time = Instant.now()
        selectOrderFrom(time)
            .expectSqlAndParams("select * from \"orders\" where \"orders\".\"created\" > ?", listOf(time))
    }
    "select orders to" {
        val time = Instant.now()
        selectOrderTo(time)
            .expectSqlAndParams("select * from \"orders\" where \"orders\".\"created\" < ?", listOf(time))
    }
    "select users orders" {
        val userId = 1L
        selectUserOrders(userId)
            .expectSqlAndParams(
                "select \"orders\".\"id\" as orderId,\"users\".\"name\",\"users\".\"id\" as userId " +
                        "from \"orders\" join \"users\" on \"users\".\"id\" = \"orders\".\"user_id\" " +
                        "where \"orders\".\"id\" = ?",
                listOf(userId)
            )
    }
    "select users orders count" {
        selectUsersOrdersCount()
            .expectSqlAndParams(
                "select \"users\".\"name\",\"users\".\"id\",count(\"orders\".*) as count " +
                        "from \"users\" join \"orders\" on \"orders\".\"user_id\" = \"users\".\"id\" " +
                        "group by \"users\".\"id\""
            )
    }
    "select users sorted by age desc and created date asc" {
        selectUsersSorted()
            .expectSqlAndParams("select * from \"users\" order by \"age\" desc,\"created\" asc")
    }
    "select users with age in ranges" {
        val ranges = listOf(
            5..10,
            16..18,
            40..60
        )
        selectUsersWithAgeInRanges(ranges)
            .expectSqlAndParams(
                "select * from \"users\" where " +
                        "(\"users\".\"age\" >= ? and \"users\".\"age\" <= ?) or " +
                        "(\"users\".\"age\" >= ? and \"users\".\"age\" <= ?) or " +
                        "(\"users\".\"age\" >= ? and \"users\".\"age\" <= ?)",
                ranges.flatMap { listOf(it.first, it.last) }
            )
    }
})
