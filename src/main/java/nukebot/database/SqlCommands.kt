package nukebot.database

import nukebot.LOGGER
import nukebot.command.Command
import nukebot.command.custom.CmdCustomCommand
import nukebot.util.statement
import java.sql.SQLException

object SqlCommands {

    @Throws(SQLException::class)
    fun create(guildID: String, name: String, answer: String) {
        statement("INSERT INTO `commands` " + "(`guild`, `name`, `answer`) VALUES (?, ?, ?)") { st ->

            st.setString(1, guildID)
            st.setString(2, name)
            st.setString(3, answer)

            st.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    fun remove(guildID: String, name: String) {
        statement("DELETE FROM `commands` WHERE (`guild`, `name`) = (?, ?)") { st ->

            st.setString(1, guildID)
            st.setString(2, name)

            st.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    fun exists(guildID: String, name: String): Boolean {
        return statement("SELECT `name` FROM `commands` WHERE (`guild`, `name`) = (?, ?)", false) { st ->

            st.setString(1, guildID)
            st.setString(2, name)

            return st.executeQuery().first()
        }
    }

    @Throws(SQLException::class)
    fun allCommands(commands: MutableMap<String, Command>) {
        statement(
                "SELECT `guild`, `name`, `answer` FROM `commands` LIMIT 0, 18446744073709551615") { st ->

            val rs = st.executeQuery() ?: return

            while (rs.next()) {
                val guildID = rs.getString("guild")
                val name = rs.getString("name")
                val answer = rs.getString("answer")

                val command = commands[name]
                when (command) {
                    null -> commands.put(name, CmdCustomCommand(guildID, answer))
                    is CmdCustomCommand -> command.put(guildID, answer)
                    else -> LOGGER.error("Unable to register command {}, existing name", name)
                }
            }
        }
    }

}
