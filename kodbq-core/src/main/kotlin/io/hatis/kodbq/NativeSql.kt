package io.hatis.kodbq

class NativeSql(
    val dialect: SqlDialect,
    val tableName: String? = null,
    private val generateFun: Generator.() -> String
) {
    fun generate(
        paramOffset: Int = 0,
        outParams: MutableList<Any?>,
        paramPlaceholder: (Int) -> String
    )  = Generator(dialect, tableName, outParams, paramOffset, paramPlaceholder).generateFun()

    class Generator(
        val dialect: SqlDialect,
        val tableName: String? = null,
        private val outParams: MutableList<Any?>,
        private val paramOffset: Int = 0,
        private val paramPlaceholder: (Int) -> String
    ) {
        fun table(tableName: String) = dialect.escape(tableName)
        fun t(tableName: String) = table(tableName)
        fun column(name: String) = Column(name, dialect, table = tableName)
        fun column(tableName: String, name: String) = Column(name, dialect, table = tableName)
        fun c(name: String) = column(name)
        fun c(tableName: String, name: String) = column(tableName, name)
        fun value(v: Any): String {
            outParams.add(v)
            return paramPlaceholder(outParams.size + paramOffset)
        }
        fun v(v: Any) = value(v)
    }
}

