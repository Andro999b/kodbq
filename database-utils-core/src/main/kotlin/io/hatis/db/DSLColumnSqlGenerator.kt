package io.hatis.db

class DSLColumnSqlGenerator(
    val usage: Usage,
    private var paramOffset: Int = 0,
    private val outParams: MutableList<Any?>,
    private val paramPlaceholder: (Int) -> String,
    private val column: Column
) {
    var generatedSql: String? = null

    fun column() = column
    fun column(name: String) = Column(name, mode = column.mode, table = column.table)

    fun c() = column
    fun c(name: String) = Column(name, mode = column.mode, table = column.table)

    fun value(v: Any): String {
        outParams.add(v)
        return paramPlaceholder(outParams.size + paramOffset)
    }

    fun v(v: Any) = value(v)

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

    data class CustomSqlPart(val actions: DSLColumnSqlGenerator.() -> Unit)
    enum class Usage { insert, update, where }
}