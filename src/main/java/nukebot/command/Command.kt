package nukebot.command

import nukebot.NuclearBot
import sx.blah.discord.handle.obj.IMessage

@FunctionalInterface interface Command {

    fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>)

}
