package nuclearcoder.discordbot.database;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nuclearcoder.discordbot.command.Command;
import nuclearcoder.discordbot.command.custom.CmdCustomCommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlCommands {

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

    public static final Multimap<String, Command> allCommands() throws SQLException
    {
        PreparedStatement statement = Database.conn.prepareStatement(
                "SELECT `guild`, `name`, `answer` FROM `commands` "
                        + "LIMIT 0, 18446744073709551615");

        ResultSet rs = statement.executeQuery();

        Multimap<String, Command> commandSet = ArrayListMultimap.create(20, 2);

        while (rs.next())
        {
            String guildID = rs.getString("guild");
            String command = rs.getString("name");
            String answer = rs.getString("answer");

            commandSet.put(command, new CmdCustomCommand(guildID, answer));
        }

        rs.close();
        statement.close();

        return commandSet;
    }

}
