package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.util.rSendMessage
import sx.blah.discord.handle.obj.IMessage

class CmdTarget

// param is (mention, target) -> (answer)
(private val answer: (String, String) -> String) : Command {

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        if (args.size >= 2) {
            val channel = message.channel
            val user = message.author

            val target = args.sliceArray(1..args.lastIndex).joinToString(" ").trim()

            channel.rSendMessage(answer(user.mention(), target))
        }
    }

}
