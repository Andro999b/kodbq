package io.hatis.kodbq

data class BuildOptions(
    val expandIn: Boolean = true,
    val generatedKeysSql: Boolean = true,
    val paramPlaceholder: (index: Int) -> String = { "?" }
)

val defaultBuildOptions = BuildOptions()