package nukebot.command.custom

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.util.rSendMessage
import nukeutils.ArgumentFormatter
import sx.blah.discord.handle.obj.IMessage
import java.util.*

class CmdCustomCommand(guildID: String, format: String) : Command {

    // used when there is 1 format
    private var singleGuildID = guildID
    private var singleFormat = ArgumentFormatter(format)

    // used when there are 2+ formats
    private var formats: MutableMap<String, ArgumentFormatter>? = null

    fun put(guildID: String, format: String) {
        if (formats == null) {
            formats = HashMap()
            formats?.put(singleGuildID, singleFormat)
        }
        formats?.put(guildID, ArgumentFormatter(format))
    }

    // returns true if empty after removal
    fun remove(guildID: String): Boolean {
        if (formats == null) return true

        formats?.remove(guildID)
        if (formats?.isEmpty() ?: true) { // if formats is empty, free it
            formats = null
        }

        return false
    }

    private fun getFormatter(guildID: String): ArgumentFormatter? {
        return if (formats == null) {
            if (guildID == singleGuildID) singleFormat else null
        } else {
            formats?.get(guildID)
        }
    }

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        val formatter = getFormatter(message.guild.id)

        if (formatter != null) {
            val author = message.author

            val formattedMessage = formatter.format(author.mention(), *args.sliceArray(1..args.lastIndex))
            if (formattedMessage != null) {
                message.channel.rSendMessage(formattedMessage)
            }
        }
    }

}
