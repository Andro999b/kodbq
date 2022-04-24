package io.hatis.kodbq


class NativeSqlColumn(
    private val column: Column,
    private val generateFun: Generator.() -> String
) {

    fun generate(
        usage: Usage,
        paramOffset: Int = 0,
        outParams: MutableList<Any?>,
        paramPlaceholder: (Int) -> String
    )  = Generator(column, usage, paramOffset, outParams, paramPlaceholder).generateFun()
    
    class Generator(
        val column: Column,
        val usage: Usage,
        private var paramOffset: Int = 0,
        private val outParams: MutableList<Any?>,
        private val paramPlaceholder: (Int) -> String
    ) {
        fun column(name: String) = Column(name, dialect = column.dialect, table = column.table)
        fun c(name: String) = Column(name, dialect = column.dialect, table = column.table)

        val c = column

        fun value(v: Any): String {
            outParams.add(v)
            return paramPlaceholder(outParams.size + paramOffset)
        }

        fun v(v: Any) = value(v)
    }

    enum class Usage { INSERT, UPDATE, CONDITION }
}