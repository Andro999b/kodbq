package io.hatis.db

class SqlGenerator(
    val usage: Usage,
    private var paramOffset: Int = 0,
    private val outParams: MutableList<Any?>,
    private val paramPlaceholder: (Int) -> String,
    private val column: Column
) {
    lateinit var generatedSql: String

    fun column() = column

    fun value(v: Any): String {
        outParams.add(v)
        return paramPlaceholder(outParams.size + paramOffset)
    }

    fun sql(s: String) {
        generatedSql = s
    }

    data class GeneratedPart(val actions: SqlGenerator.() -> Unit)
    enum class Usage { insert, update, where }
}