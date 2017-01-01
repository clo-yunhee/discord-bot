package nukebot.util

import nukebot.GET_TIMEOUT
import nukebot.LOGGER
import nukebot.database.SqlUsers
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import java.io.IOException
import java.net.URL
import java.sql.SQLException


// TODO: have a cache for operators, so we don't have to query the DB every time

fun isOperator(userID: String): Boolean {
    return true
    return try {
        SqlUsers.isOperator(userID)
    } catch (e: SQLException) {
        LOGGER.error("Could not check operator", e)
        false
    }
}

fun setOperator(userID: String, isOp: Boolean) {
    try {
        SqlUsers.setOperator(userID, if (isOp) 1 else 0)
    } catch (e: SQLException) {
        LOGGER.error("Could not set operator", e)
    }
}

fun IUser.hasPermission(guild: IGuild, perm: Permissions): Boolean {
    return getRolesForGuild(guild).any {
        Permissions.ADMINISTRATOR in it.permissions || perm in it.permissions
    }
}

fun IChannel.rSendMessage(content: String) {
    RequestBuffer.request {
        try {
            sendMessage(content)
        } catch (e: MissingPermissionsException) {
            LOGGER.error("Could not send message", e)
        } catch (e: DiscordException) {
            LOGGER.error("Could not send message", e)
        }
    }
}

fun IMessage.rReply(content: String) {
    RequestBuffer.request {
        try {
            reply(content)
        } catch (e: MissingPermissionsException) {
            LOGGER.error("Could not reply to message", e)
        } catch (e: DiscordException) {
            LOGGER.error("Could not reply to message", e)
        }
    }
}

fun IChannel.rSendFileURL(content: String, url: String,
                          filename: String) {
    try {
        val conn = URL(url).openConnection()
        conn.doInput = true
        conn.doOutput = true
        conn.readTimeout = GET_TIMEOUT
        conn.connectTimeout = GET_TIMEOUT

        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11")

        conn.inputStream.use {
            RequestBuffer.request {
                try {
                    sendFile(content, false, it, filename)
                } catch (e: MissingPermissionsException) {
                    LOGGER.error("Could not send file", e)
                } catch (e: DiscordException) {
                    LOGGER.error("Could not send file", e)
                }
            }
        }
    } catch (e: IOException) {
        LOGGER.error("Could not send file:", e)
    }

}