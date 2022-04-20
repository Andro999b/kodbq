package io.hatis.db.test

import io.hatis.db.QueryBuilder
import io.hatis.db.SqlBuilder
import io.kotest.core.spec.style.funSpec

class TestCase(val name: String) {
    lateinit var query: SqlBuilder
    lateinit var verify: (query: SqlBuilder) -> Unit
}

infix fun String.testCase(block: TestCase.() -> Unit) = TestCase(this).apply(block)

fun testSuit(env: String, testCases: Collection<TestCase>) = funSpec {
    context(env) {
        testCases.forEach {
            test(it.name) { it.verify(it.query) }
        }
    }
}