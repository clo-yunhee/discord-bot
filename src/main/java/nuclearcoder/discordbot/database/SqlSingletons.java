package nuclearcoder.discordbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlSingletons {

    public static final void ensureExists() throws SQLException
    {
        PreparedStatement statement = Database.conn.prepareStatement("SELECT `_` FROM `singleton`");

        ResultSet rs = statement.executeQuery();
        boolean hasFirst = rs.first();
        rs.close();
        if (!hasFirst)
        {
            statement.executeUpdate(
                    "INSERT INTO `singleton` (`_`, `cahchannel`, `cahdecks`, `cahconfig`) VALUES (0, '', '', '')");
        }

        statement.close();
    }

}
