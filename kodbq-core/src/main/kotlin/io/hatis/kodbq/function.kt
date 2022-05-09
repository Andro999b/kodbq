package io.hatis.kodbq

data class NativeFunction(val nativeSqlColumn: NativeSql) : Function

data class SimpleFunction(val function: String, val column: Column) : Function, Named {
    override val escapeName: String
        get() {
            val columnName = if (column.isStar) "*" else column.toString()
            return "$function($columnName)"
        }

    override fun toString(): String = escapeName
}