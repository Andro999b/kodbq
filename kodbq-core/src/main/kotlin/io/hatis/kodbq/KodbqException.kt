package io.hatis.kodbq

class KodbqException: RuntimeException {
    constructor(cause: Throwable): super(cause)
    constructor(msg: String): super(msg)
}
