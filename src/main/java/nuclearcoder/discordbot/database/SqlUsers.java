package nuclearcoder.discordbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlUsers {

    public static final void createUser(String userID) throws SQLException
    {
        PreparedStatement statement = Database.conn
                .prepareStatement("INSERT INTO `users` (`uid`) VALUES (?)");

        statement.setString(1, userID);

        statement.executeUpdate();

        statement.close();
    }

    public static final boolean isUserPresent(String userID) throws SQLException
    {
        PreparedStatement statement = Database.conn
                .prepareStatement("SELECT NULL FROM `users` WHERE `uid` = ?");

        statement.setString(1, userID);

        ResultSet rs = statement.executeQuery();
        boolean isPresent = rs.first();

        rs.close();
        statement.close();

        return isPresent;
    }

    public static final void setOperator(String userID, int value) throws SQLException
    {
        PreparedStatement statement = Database.conn
                .prepareStatement("UPDATE `users` SET `operator` = ? WHERE `uid` = ?");

        statement.setInt(1, value);
        statement.setString(2, userID);

        statement.executeUpdate();

        statement.close();
    }

    public static final boolean isOperator(String userID) throws SQLException
    {
        PreparedStatement statement = Database.conn
                .prepareStatement("SELECT `operator` FROM `users` WHERE `uid` = ?");

        statement.setString(1, userID);

        ResultSet rs = statement.executeQuery();
        boolean isOperator = rs.first() && (rs.getInt(1) != 0);

        rs.close();
        statement.close();

        return isOperator;
    }

}
