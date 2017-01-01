package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.rSendMessage
import sx.blah.discord.handle.obj.IMessage
import java.util.*

class CmdPat : Command {

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        if (args.size >= 2) {
            val channel = message.channel
            val user = message.author

            val target = Arrays.copyOfRange(args, 1, args.size).joinToString(" ").trim { it <= ' ' }

            channel.rSendMessage(user.mention() + " pats " + target)
        }
    }

}
