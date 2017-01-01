package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.rReply
import sx.blah.discord.handle.obj.IMessage

class CmdPing : Command {

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        message.rReply("pong!")
    }

}
