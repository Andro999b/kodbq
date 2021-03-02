package io.hatis.db

data class Column(
    val name: String,
    val mode: SqlMode,
    val table: String? = null
) {
    private fun escapeName()  = if(name == "*") "*" else mode.escape(name)

    override fun toString(): String {
        if(table != null) {
            return "${mode.escape(table)}.${escapeName()}"
        } else {
            return escapeName()
        }
    }
}
