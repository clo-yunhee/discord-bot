package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.util.rSendMessage
import sx.blah.discord.handle.obj.IMessage

class CmdAnswer

// param is (mention) -> (answer)
(private val answer: (String) -> String) : Command {

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        val channel = message.channel
        val user = message.author

        channel.rSendMessage(answer(user.mention()))
    }

}
