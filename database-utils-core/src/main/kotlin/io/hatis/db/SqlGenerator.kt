package io.hatis.db

class SqlGenerator(
    val usage: Usage,
    private var paramOffset: Int = 0,
    private val outParams: MutableList<Any?>,
    private val paramPlaceholder: (Int) -> String,
    private val column: Column
) {
    var generatedSql: String? = null

    fun column() = column
    fun column(name: String) = Column(name, mode = column.mode, table = column.table)

    fun value(v: Any): String {
        outParams.add(v)
        return paramPlaceholder(outParams.size + paramOffset)
    }

    fun updateSql(s: String) {
        if(usage == Usage.update) {
            generatedSql = s
        }
    }

    fun insertSql(s: String) {
        if(usage == Usage.insert) {
            generatedSql = s
        }
    }

    fun whereSql(s: String) {
        if(usage == Usage.where) {
            generatedSql = s
        }
    }

    data class GeneratedPart(val actions: SqlGenerator.() -> Unit)
    enum class Usage { insert, update, where }
}