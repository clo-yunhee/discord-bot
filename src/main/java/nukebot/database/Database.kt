package nukebot.database

import nukebot.DB_DRIVER
import nukeutils.Config
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

object Database {

    var conn: Connection? = null

    val LOGGER: Logger = LoggerFactory.getLogger(Database::class.java)

    fun loadDriver() {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName(DB_DRIVER).newInstance()
        } catch (e: Exception) {
            when (e) {
                is InstantiationException,
                is IllegalAccessError,
                is ClassNotFoundException
                -> LOGGER.error("Could not load DB driver", e)
                else -> throw e
            }
        }
    }

    fun openConnection() {
        val host = Config["db_host"]
        val port = Config["db_port"]

        val user = Config["db_user"]
        val pass = Config["db_pass"]

        val schema = Config["db_schema"]

        val connectionProps = Properties()
        connectionProps.putAll(mapOf(
                "user" to user,
                "password" to pass,
                "useSSL" to "false",
                "autoReconnect" to "true"
        ))

        try {
            conn = DriverManager.getConnection("jdbc:mysql://$host:$port/$schema",
                    connectionProps)
        } catch (e: SQLException) {
            LOGGER.error("Could not open DB connection", e)
            conn = null
        }
    }

    fun closeConnection() {
        try {
            conn?.close()
        } catch (e: SQLException) {
            LOGGER.error("Could not close DB connection", e)
        }
    }

    val connected: Boolean
        get() {
            return try {
                !(conn?.isClosed ?: true)
            } catch (e: SQLException) {
                LOGGER.error("Could not query DB state", e)
                false
            }
        }

    fun keepAlive() {
        var st: Statement? = null
        try {
            st = conn?.createStatement()
            st?.execute("SELECT null FROM singleton")
        } catch (e: SQLException) {
            LOGGER.error("Could not keep alive", e)
        } finally {
            st?.close()
        }
    }
}
