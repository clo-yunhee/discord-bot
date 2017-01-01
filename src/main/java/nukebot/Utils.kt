package nukebot

import nukebot.database.SqlUsers
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import java.io.IOException
import java.net.URL
import java.security.cert.X509Certificate
import java.sql.SQLException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal fun hackSslVerifier() {
    // FIXME: My JVM doesn't like the certificate. I should go add
    // StartSSL's root certificate to
    // its trust store, and document steps. For now, I'm going to disable
    // SSL certificate checking.

    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }

        override fun checkClientTrusted(certs: Array<X509Certificate>,
                                        authType: String) {
        }

        override fun checkServerTrusted(certs: Array<X509Certificate>,
                                        authType: String) {
        }
    })

    try {
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
    } catch (e: Exception) {
        LOGGER.error("Unable to install trust-all security manager", e)
    }

    // Create host name verifier that only trusts our chosen host names
    HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> hostname in TRUSTED_HOST_NAMES }
}

fun IUser.hasPermission(guild: IGuild, perm: Permissions): Boolean {
    return getRolesForGuild(guild).any {
        Permissions.ADMINISTRATOR in it.permissions || perm in it.permissions
    }
}

fun isOperator(userID: String): Boolean {
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

fun stringFromTime(time: Int): String {
    val sec = time % 60
    val min = (time / 60) % 60
    val hrs = time / 3600

    val sb = StringBuilder()

    if (hrs > 0) {
        if (hrs < 10)
            sb.append('0')
        sb.append(hrs)
        sb.append(':')
    }

    if (min < 10)
        sb.append('0')
    sb.append(min)
    sb.append(':')

    if (sec < 10)
        sb.append('0')
    sb.append(sec)

    return sb.toString()
}

fun parseTime(string: String): Int {
    // that is probably an awful way to parse time

    val parts = string.split(':')

    if (parts.size !in 2..3)
        throw NumberFormatException("Not a time format")

    var i: Int = 0

    val hrs = if (parts.size < 3) 0 else Integer.parseUnsignedInt(parts[i++])
    val min = Integer.parseUnsignedInt(parts[i++])
    val sec = Integer.parseUnsignedInt(parts[i])

    if (min >= 60 || sec >= 60)
        throw NumberFormatException("Not a time format")

    return (hrs * 60 + min) * 60 + sec
}
