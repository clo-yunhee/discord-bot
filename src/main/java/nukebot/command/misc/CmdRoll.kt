package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.rSendMessage
import sx.blah.discord.handle.obj.IMessage
import java.util.*

class CmdRoll : Command {

    private val random = Random()

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        var bound = 100
        try {
            if (args.size >= 2)
                bound = Integer.parseUnsignedInt(args[1])
        } catch (e: NumberFormatException) {
            // whatever
        }

        try {
            val roll = random.nextInt(bound) + 1

            val channel = message.channel
            val user = message.author

            channel.rSendMessage(user.mention() + " rolled " + roll + ".")
        } catch (e: IllegalArgumentException) {
            // what. ever.
        }

    }

}
