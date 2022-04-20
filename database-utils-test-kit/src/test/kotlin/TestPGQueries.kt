import io.hatis.db.test.*
import io.kotest.core.spec.style.FunSpec
import java.time.Instant

class TestPGQueries: FunSpec({
    include(testSuit("pg queries", listOf(
        "select all" testCase {
            query = selectAllUsers()
            verify = expectSqlAndParams("select * from \"users\"")
        },
        "count users" testCase {
            query = countUsers()
            verify = expectSqlAndParams("select count(*) as \"count\" from \"users\"")
        },
        "select user with id" testCase {
            val userId = 1L
            query = selectByUserId(userId)
            verify = expectSqlAndParams("select * from \"users\" where \"users\".\"id\" = ?", listOf(userId))
        },
        "select user with id collection" testCase {
            val userIds: List<Long> = listOf(1, 2, 3, 4)
            query = selectByUserIds(userIds)
            verify = expectSqlAndParams("select * from \"users\" where \"users\".\"id\" = any(?)", listOf(userIds))
        },
        "select with offset" testCase {
            query = selectUsersWithOffset(100, 10)
            verify = expectSqlAndParams("select * from \"users\" offset 100 limit 10")
        },
        "select with range" testCase {
            query = selectUsersInRange(100, 110)
            verify = expectSqlAndParams("select * from \"users\" offset 100 limit 10")
        },
        "select users age between" testCase {
            query = selectUserAgeBetween(18, 25)
            verify = expectSqlAndParams(
                "select * from \"users\" where (\"users\".\"age\" >= ? and \"users\".\"age\" <= ?)",
                listOf(18, 25)
            )
        },
        "select users with name like" testCase {
            query = selectUserWithNameLike("mike")
            verify = expectSqlAndParams(
                "select * from \"users\" where \"users\".\"name\" like ?",
                listOf("mike")
            )
        },
        "select deleted users" testCase {
            query = selectDeletedUsers()
            verify = expectSqlAndParams("select * from \"users\" where \"users\".\"deleted\" is not null")
        },
        "select users age and name" testCase {
            query = selectUsersNameAndAge()
            verify = expectSqlAndParams("select \"users\".\"name\",\"users\".\"age\" from \"users\"")
        },
        "select orders from now" testCase {
            val time = Instant.now()
            query = selectOrderFrom(time)
            verify = expectSqlAndParams("select * from \"orders\" where \"orders\".\"created\" > ?", listOf(time))
        },
        "select orders to" testCase {
            val time = Instant.now()
            query = selectOrderTo(time)
            verify = expectSqlAndParams("select * from \"orders\" where \"orders\".\"created\" < ?", listOf(time))
        },
        "select users orders" testCase {
            val userId = 1L
            query = selectUserOrders(userId)
            verify = expectSqlAndParams(
                "select \"orders\".\"id\" as orderId,\"users\".\"name\" as name,\"users\".\"id\" as userId " +
                        "from \"orders\" join \"users\" on \"users\".\"id\" = \"orders\".\"user_id\" " +
                        "where \"orders\".\"id\" = ?",
                listOf(userId)
            )
        },
        "select users orders count" testCase {
            query = selectUsersOrdersCount()
            verify = expectSqlAndParams("")
        }
    )))
})
