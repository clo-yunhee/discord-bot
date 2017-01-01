package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.rSendMessage
import sx.blah.discord.handle.obj.IMessage

class CmdKiss : Command {

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        if (args.size >= 2) {
            val channel = message.channel
            val user = message.author

            val target = args.sliceArray(1..args.lastIndex).joinToString(" ").trim()

            channel.rSendMessage(user.mention() + " kisses " + target + "  ( \u02D8 \u00B3\u02D8) ~~")
        }
    }

}
