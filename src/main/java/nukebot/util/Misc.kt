package nukebot.util

import nukebot.LOGGER
import nukebot.TRUSTED_HOST_NAMES
import java.security.SecureRandom
import java.security.cert.X509Certificate
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
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
    } catch (e: Exception) {
        LOGGER.error("Unable to install trust-all security manager", e)
    }

    // Create host name verifier that only trusts our chosen host names
    HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> hostname in TRUSTED_HOST_NAMES }
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
