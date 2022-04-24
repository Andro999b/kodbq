package io.hatis.kodbq

class Column(
    val name: String,
    val dialect: SqlDialect,
    val table: String? = null,
    val alias: String? = null
) {
    fun escapeName()  = if(name == "*") "*" else dialect.escape(name)
    fun escapeTable() = table?.let { dialect.escape(table) }

    override fun toString(): String {
        return if(table != null) {
            "${dialect.escape(table)}.${escapeName()}"
        } else {
            escapeName()
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
