package io.hatis.db

data class Column(
    val name: String,
    val mode: SqlMode,
    val table: String? = null
) {
    override fun toString(): String {
        if(table != null) {
            return "${mode.escape(table)}.${mode.escape(name)}"
        } else {
            return mode.escape(name)
        }
    }
}
