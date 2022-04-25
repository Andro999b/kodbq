package io.hatis.kodbq

data class NativeFunction(val nativeSqlColumn: NativeSql) : Function

data class SimpleFunction(val function: String, val column: Column) : Function, Named {
    override fun escapeName() = "$function($column)"

    override fun toString(): String = escapeName()
}