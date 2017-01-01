package nukebot.database

import nukebot.LOGGER
import nukebot.command.Command
import nukebot.command.custom.CmdCustomCommand
import java.sql.SQLException

object SqlCommands {

    @Throws(SQLException::class)
    fun create(guildID: String, name: String, answer: String) {
        val statement = Database.conn?.prepareStatement(
                "INSERT INTO `commands` " + "(`guild`, `name`, `answer`) " + "VALUES "
                        + "(?, ?, ?)") ?: return

        statement.setString(1, guildID)
        statement.setString(2, name)
        statement.setString(3, answer)

        statement.executeUpdate()

        statement.close()
    }

    @Throws(SQLException::class)
    fun remove(guildID: String, name: String) {
        val statement = Database.conn
                ?.prepareStatement("DELETE FROM `commands` WHERE (`guild`, `name`) = (?, ?)") ?: return

        statement.setString(1, guildID)
        statement.setString(2, name)

        statement.executeUpdate()

        statement.close()
    }

    @Throws(SQLException::class)
    fun exists(guildID: String, name: String): Boolean {
        val statement = Database.conn
                ?.prepareStatement("SELECT `name` FROM `commands` WHERE (`guild`, `name`) = (?, ?)") ?: return false

        statement.setString(1, guildID)
        statement.setString(2, name)

        val rs = statement.executeQuery()
        val exists = rs.first()

        rs.close()
        statement.close()

        return exists
    }

    @Throws(SQLException::class)
    fun allCommands(commands: MutableMap<String, Command>) {
        val statement = Database.conn?.prepareStatement(
                "SELECT `guild`, `name`, `answer` FROM `commands` " + "LIMIT 0, 18446744073709551615") ?: return

        val rs = statement.executeQuery() ?: return

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

        rs.close()
        statement.close()
    }

}
