package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.rSendFileURL
import sx.blah.discord.handle.obj.IMessage

class CmdUwu : Command {

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        message.channel.rSendFileURL("", "https://puu.sh/sAQuH/29cca0f3cd.jpg", "uwu.jpg")
    }

}
