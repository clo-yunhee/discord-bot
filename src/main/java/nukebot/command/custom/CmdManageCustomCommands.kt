package nukebot.command.custom

import nukebot.*
import nukebot.command.Command
import nukebot.command.CommandManager
import nukebot.database.SqlCommands
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.Permissions
import java.sql.SQLException

class CmdManageCustomCommands(private val commandsManager: CommandManager) : Command {

    private fun register(guildID: String, name: String, answer: String) {
        val command = commandsManager.getCommand(name)
        when (command) {
            null -> commandsManager.putCommand(name, CmdCustomCommand(guildID, answer))
            is CmdCustomCommand -> command.put(guildID, answer)
            else -> {
                LOGGER.error("Unable to register command $name, existing name")
                return
            }
        }

        // only reached if a command was added
        try {
            SqlCommands.create(guildID, name, answer)
        } catch (e: SQLException) {
            LOGGER.error("Could not create command", e)
        }

    }

    private fun unregister(guildID: String, name: String) {
        val command = commandsManager.getCommand(name)
        if (command != null && command is CmdCustomCommand) {
            if (command.remove(guildID))
                commandsManager.removeCommand(guildID)

            try {
                SqlCommands.remove(guildID, name)
            } catch (e: SQLException) {
                LOGGER.error("Could not remove command", e)
            }

        }
    }

    private fun commandExists(guildID: String, name: String): Boolean {
        return try {
            SqlCommands.exists(guildID, name)
        } catch (e: SQLException) {
            false
        }
    }

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        val user = message.author
        val guild = message.guild

        if (!user.hasPermission(guild, Permissions.MANAGE_MESSAGES)) {
            message.rReply("you need a guild-wide \"Manage Messages\" permission to (un-)register a command. :warning:")
        } else {
            val guildID = guild.id

            if (command.equals("add_cmd", ignoreCase = true)) {
                if (args.size <= 2) {
                    message.rReply("you must provide command name and reply. :warning:")
                } else {
                    val argCommand = args[1]

                    if (commandExists(guildID, argCommand)) {
                        message.rReply("this command already exists. You can un-register if it's a custom command. :warning:")
                    } else {
                        val argAnswer = args.sliceArray(2..args.lastIndex).joinToString(" ")

                        register(guildID, argCommand, argAnswer)

                        message.channel.rSendMessage("the `$argCommand` command was created successfully. :white_check_mark:")
                    }
                }
            } else if (command.equals("rem_cmd", ignoreCase = true)) {
                if (args.size <= 1) {
                    message.rReply("you must provide command name. :warning:")
                } else {
                    val argCommand = args[1]

                    if (!commandExists(guildID, argCommand)) {
                        message.rReply("this command does not exist or is reserved. You can register if it's a custom command. :warning:")
                    } else {
                        unregister(guildID, argCommand)

                        message.channel.rSendMessage("the `$argCommand` command was removed successfully. :white_check_mark:")
                    }
                }
            }
        }
    }

}