package io.hatis.db

class Column(
    val name: String,
    val mode: SqlMode,
    val table: String? = null,
    val alias: String? = null
) {
    fun escapeName()  = if(name == "*") "*" else mode.escape(name)
    fun escapeTable() = table?.let { mode.escape(table) }

    override fun toString(): String {
        if(table != null) {
            return "${mode.escape(table)}.${escapeName()}"
        } else {
            return escapeName()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Column

        if (name != other.name) return false
        if (table != other.table) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (table?.hashCode() ?: 0)
        return result
    }
}
