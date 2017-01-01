package nukebot.command

import com.vdurmont.emoji.EmojiParser
import nukebot.LOGGER
import nukebot.NuclearBot
import nukebot.command.custom.CmdManageCustomCommands
import nukebot.command.manage.CmdManageBot
import nukebot.command.misc.*
import nukebot.database.Database
import nukebot.database.SqlCommands
import nukebot.database.SqlUsers
import sx.blah.discord.handle.impl.events.MessageReceivedEvent
import java.sql.SQLException
import java.util.*

class CommandManagerImpl(private val bot: NuclearBot) : CommandManager {

    private val commands: MutableMap<String, Command> = HashMap()

    override fun initCommands() {
        // system commands
        val manageBot = CmdManageBot()
        val manageCustomCommands = CmdManageCustomCommands(this)
        commands.put("stop", manageBot)
        commands.put("restart", manageBot)
        commands.put("set_op", manageBot)
        commands.put("reload", manageBot)
        commands.put("add_cmd", manageCustomCommands)
        commands.put("rem_cmd", manageCustomCommands)

        // pre-built commands
        commands.put("roll", CmdRoll())
        commands.put("hug", CmdHug())
        commands.put("kiss", CmdKiss())
        commands.put("pat", CmdPat())
        commands.put("lick", CmdLick())
        commands.put("uwu", CmdUwu())
        commands.put("8ball", Cmd8ball())
        commands.put("ping", CmdPing())

        // init custom commands
        try {
            LOGGER.info("Registering custom commands")

            SqlCommands.allCommands(commands)
        } catch (e: SQLException) {
            LOGGER.error("SQL error:", e)
        }

    }

    override fun clearCommands() {
        commands.clear()
    }

    override fun handle(event: MessageReceivedEvent) {
        val message = event.message
        val content = EmojiParser.parseFromUnicode(message.content) { ':' + it.emoji.aliases[0] + ':' }

        if (content.startsWith(CommandManager.COMMAND_PREFIX)) {
            val args = content.substring(1).split("\\s+".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val name = args[0]

            val command = commands[name] ?: return

            if (Database.connected) {
                /* add the player to the database */
                try {
                    val userID = message.author.id
                    if (!SqlUsers.isUserPresent(userID))
                        SqlUsers.createUser(userID)
                } catch (e: SQLException) {
                    LOGGER.error("Couldn't init player entry:", e)
                }

            }

            command.execute(bot, message, name, args)
        }
    }

    override fun getCommand(label: String): Command? {
        return commands[label]
    }

    override fun putCommand(label: String, command: Command) {
        commands.put(label, command)
    }

    override fun removeCommand(label: String) {
        commands.remove(label)
    }

}
