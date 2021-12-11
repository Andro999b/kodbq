package io.hatis.db

class QueryBuilder(
    private  val mode: SqlMode,
    private val generatorActions: DSLQueryGenerator.() -> Unit
) : SqlBuilder {
    override fun buildSqlAndParams(paramPlaceholder: (Int) -> String): Pair<String, List<Any?>> {
        val outParams = mutableListOf<Any?>()
        val generator = DSLQueryGenerator(mode, outParams, paramPlaceholder)
        generator.generatorActions()

        return generator.generate() to outParams
    }
}