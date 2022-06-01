package io.hatis.kodbq

class NativeSql(
    val dialect: SqlDialect,
    private val generateFun: Generator.() -> String
) {
    fun generate(
        paramOffset: Int = 0,
        outParams: MutableList<Any?>,
        paramPlaceholder: (Int) -> String
    )  = Generator(dialect, outParams, paramOffset, paramPlaceholder).generateFun()

    class Generator(
        val dialect: SqlDialect,
        private val outParams: MutableList<Any?>,
        private val paramOffset: Int = 0,
        private val paramPlaceholder: (Int) -> String
    ) {
        fun table(td: Table) = dialect.escape(td.name)
        fun t(td: Table) = table(td)
        fun column(cd: ColumnDefinition) = cd.toColunm(dialect)
        fun c(cd: ColumnDefinition) = column(cd)
        fun value(v: Any): String {
            outParams.add(v)
            return paramPlaceholder(outParams.size + paramOffset)
        }
        fun v(v: Any) = value(v)
    }
}

