package io.hatis.kodbq.test

import io.hatis.kodbq.Column
import io.hatis.kodbq.SqlDialect
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TestColumnEquals: StringSpec({
    "test column equals()" {
        val a = Column("col", dialect = SqlDialect.MY_SQL, "t1")
        val b = Column("col", dialect = SqlDialect.PG, "t1")


        val c1 = Column("col", dialect = SqlDialect.MY_SQL)
        val c2 = Column("col", dialect = SqlDialect.MY_SQL, "t2")
        val c3 = Column("col1", dialect = SqlDialect.MY_SQL, "t1")

        a shouldBe b
        b shouldBe a

        a shouldNotBe c1
        a shouldNotBe c2
        a shouldNotBe c3
    }
})