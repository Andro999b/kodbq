package io.hatis.kodbq


class NativeSqlColumn(
    private val column: Column,
    private val generateFun: Generator.() -> String?
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
        val dialect = column.dialect
        fun column(name: String) = Column(name, column.dialect, table = column.table)
        fun column(tableName: String, name: String) = Column(name, column.dialect, table = tableName)
        fun c(name: String) = column(name)

        fun c(tableName: String, name: String) = column(tableName, name)

        val c = column

        fun value(v: Any): String {
            outParams.add(v)
            return paramPlaceholder(outParams.size + paramOffset)
        }

        fun v(v: Any) = value(v)
    }

    enum class Usage { INSERT, UPDATE, CONDITION }
}