package nukebot.util

import nukebot.database.Database
import java.sql.PreparedStatement


// overload of stdlib.use for AutoCloseable

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            this.close()
        } catch (closeException: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            this.close()
        }
    }
}


// shortcut to create and use a statement

inline fun statement(query: String, block: (PreparedStatement) -> Unit) {
    (Database.conn?.prepareStatement(query) ?: return).use(block)
}

// (can't use default arguments if return type is Unit.)

inline fun <R> statement(query: String, returnIfNull: R, block: (PreparedStatement) -> R): R {
    return (Database.conn?.prepareStatement(query) ?: return returnIfNull).use(block)
}
