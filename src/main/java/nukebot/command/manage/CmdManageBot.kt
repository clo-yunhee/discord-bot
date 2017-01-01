package nukebot.command.manage

import nukebot.LOGGER
import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.reloadModules
import nukebot.util.isOperator
import nukebot.util.rSendMessage
import nukebot.util.setOperator
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.RequestBuffer

class CmdManageBot : Command {

    override fun execute(bot: NuclearBot, message: IMessage,
                         command: String, args: Array<String>) {
        val user = message.author

        if (isOperator(user.id)) {
            RequestBuffer.request {
                try {
                    val pmChannel = user.orCreatePMChannel

                    if (command.equals("stop", ignoreCase = true)) {
                        bot.terminate(false)
                    } else if (command.equals("restart", ignoreCase = true)) {
                        bot.terminate(true)
                    } else if (command.equals("setop", ignoreCase = true)) {
                        if (args.size > 2) {
                            val setOp = args[1].toBoolean()
                            val notification = "You are " + (if (setOp) "now" else "no longer") + " operator for NuclearBot. :warning: "

                            val sb = StringBuilder()
                            sb.append(if (setOp) "Set" else "Un-set")
                            sb.append(" to operator:")

                            for (mentioned in message.mentions) {
                                // set op
                                setOperator(mentioned.id, setOp)
                                // notify new op
                                mentioned.orCreatePMChannel.rSendMessage(notification)
                                sb.append("\n**")
                                sb.append(mentioned.name)
                                sb.append("**")
                            }

                            pmChannel.rSendMessage(sb.toString())
                            // notify executor
                        }
                    } else if (command.equals("reload", ignoreCase = true)) {
                        reloadModules(bot.moduleLoader)
                    }
                } catch (e: DiscordException) {
                    LOGGER.error("Error in bot management command:", e)
                }
            }
        }
    }

}
