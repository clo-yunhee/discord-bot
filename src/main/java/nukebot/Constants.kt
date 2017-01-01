package nukebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit


val LOGGER: Logger = LoggerFactory.getLogger(NuclearBot::class.java)

val TRUSTED_HOST_NAMES = setOf(
        "api.cardcastgame.com",
        "puu.sh"
)

val DB_DRIVER = "com.mysql.jdbc.Driver"

val CONFIG_FILENAME = "nuclearbot.cfg"
val MODULES_DIR = File("modules")

val GET_TIMEOUT: Int = TimeUnit.SECONDS.toMillis(3).toInt()
val KEEP_ALIVE: Long = TimeUnit.SECONDS.toMillis(5)