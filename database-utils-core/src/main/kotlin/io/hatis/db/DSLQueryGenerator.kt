package io.hatis.db

class DSLQueryGenerator(
    private val mode: SqlMode,
    private val outParams: MutableList<Any?>,
    private val paramPlaceholder: (Int) -> String
) {
    var generatedSql = StringBuilder()
    fun table(tableName: String) = mode.escape(tableName)
    fun t(tableName: String) = table(tableName)
    fun column(name: String) = Column(name, mode)
    fun column(tableName: String, name: String) = Column(name, mode, table = tableName)
    fun c(name: String) = column(name)
    fun c(tableName: String, name: String) = column(tableName, name)
    fun value(v: Any): String {
        outParams.add(v)
        return paramPlaceholder(outParams.size)
    }
    fun v(v: Any) = value(v)
    fun query(str: String) {
        generatedSql.append(str)
    }
    fun q(str: String) = query(str)
    internal fun generate() = generatedSql.toString()
}