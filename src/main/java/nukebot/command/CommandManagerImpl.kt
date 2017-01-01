package nukebot.command

import com.vdurmont.emoji.EmojiParser
import nukebot.LOGGER
import nukebot.NuclearBot
import nukebot.command.custom.CmdManageCustomCommands
import nukebot.command.manage.CmdManageBot
import nukebot.command.misc.Cmd8ball
import nukebot.command.misc.CmdAnswer
import nukebot.command.misc.CmdRoll
import nukebot.command.misc.CmdTarget
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
        commands["stop"] = manageBot
        commands["restart"] = manageBot
        commands["setop"] = manageBot
        commands["reload"] = manageBot
        commands["add_cmd"] = manageCustomCommands
        commands["rem_cmd"] = manageCustomCommands

        // pre-built commands
        commands["hug"] = CmdTarget { user, target -> "$user hugs $target  (\u3064\u2267\u25BD\u2266)\u3064" }
        commands["kiss"] = CmdTarget { user, target -> "$user kisses $target  ( \u02D8 \u00B3\u02D8) ~~" }
        commands["lick"] = CmdTarget { user, target -> "$user licks $target  ('\u30FB\u03C9\u30FB')" }
        commands["pat"] = CmdTarget { user, target -> "$user pats $target" }
        commands["uwu"] = CmdAnswer { user -> "uwu" }
        commands["ping"] = CmdAnswer { user -> "Pong!" }
        commands["roll"] = CmdRoll()
        commands["8ball"] = Cmd8ball()

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
        commands[label] = command
    }

    override fun removeCommand(label: String) {
        commands.remove(label)
    }

}
