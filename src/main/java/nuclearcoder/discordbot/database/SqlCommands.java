package nuclearcoder.discordbot.database;

import nuclearcoder.discordbot.command.Command;
import nuclearcoder.discordbot.command.custom.CmdCustomCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SqlCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlCommands.class);

    public static final void create(String guildID, String name, String answer) throws SQLException
    {
        PreparedStatement statement = Database.conn.prepareStatement(
                "INSERT INTO `commands` " + "(`guild`, `name`, `answer`) " + "VALUES "
                        + "(?, ?, ?)");

        statement.setString(1, guildID);
        statement.setString(2, name);
        statement.setString(3, answer);

        statement.executeUpdate();

        statement.close();
    }

    public static final void remove(String guildID, String name) throws SQLException
    {
        PreparedStatement statement = Database.conn
                .prepareStatement("DELETE FROM `commands` WHERE (`guild`, `name`) = (?, ?)");

        statement.setString(1, guildID);
        statement.setString(2, name);

        statement.executeUpdate();

        statement.close();
    }

    public static final boolean exists(String guildID, String name) throws SQLException
    {
        PreparedStatement statement = Database.conn
                .prepareStatement("SELECT `name` FROM `commands` WHERE (`guild`, `name`) = (?, ?)");

        statement.setString(1, guildID);
        statement.setString(2, name);

        ResultSet rs = statement.executeQuery();
        boolean exists = rs.first();

        rs.close();
        statement.close();

        return exists;
    }

    public static final void allCommands(Map<String, Command> commands) throws SQLException
    {
        PreparedStatement statement = Database.conn.prepareStatement(
                "SELECT `guild`, `name`, `answer` FROM `commands` "
                        + "LIMIT 0, 18446744073709551615");

        ResultSet rs = statement.executeQuery();

        while (rs.next())
        {
            String guildID = rs.getString("guild");
            String name = rs.getString("name");
            String answer = rs.getString("answer");

            Command command = commands.get(name);
            if (command == null)
            {
                commands.put(name, new CmdCustomCommand(guildID, answer));
            }
            else if (command instanceof CmdCustomCommand)
            {
                ((CmdCustomCommand) command).put(guildID, answer);
            }
            else
            {
                LOGGER.error("Unable to register command {}, existing name", name);
            }
        }

        rs.close();
        statement.close();
    }

}
