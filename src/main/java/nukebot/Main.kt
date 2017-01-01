package nukebot

import nukebot.database.Database
import nukeutils.Config
import sx.blah.discord.modules.Configuration

@Throws(Exception::class)
fun main(args: Array<String>) {
    hackSslVerifier() // before everything else

    Database.loadDriver() // load MySQL connector
    Database.openConnection()

    Config.reloadConfig()

    Configuration.LOAD_EXTERNAL_MODULES = false
    Configuration.AUTOMATICALLY_ENABLE_MODULES = false

    val bot = NuclearBot(Config["token"])
    bot.login()
}