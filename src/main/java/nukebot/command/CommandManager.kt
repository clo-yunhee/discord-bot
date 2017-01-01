package nukebot.command

import nukeutils.Config
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.MessageReceivedEvent

interface CommandManager : IListener<MessageReceivedEvent> {

    companion object {
        val COMMAND_PREFIX: String = Config["command_prefix"]
    }

    fun initCommands()

    fun clearCommands()

    override fun handle(event: MessageReceivedEvent)

    fun getCommand(label: String): Command?

    fun putCommand(label: String, command: Command)

    fun removeCommand(label: String)

}