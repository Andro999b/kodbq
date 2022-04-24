package io.hatis.kodbq

interface Function
data class NativeFunction(val nativeSqlColumn: NativeSql) : Function
data class SimpleFunction(val function: String, val column: Column) : Function