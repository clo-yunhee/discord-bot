package nuclearcoder.discordbot.database;

import nuclearcoder.discordbot.cah.card.CahCardDeck;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.StringTokenizer;

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

    public static class Cah {

        public static final String getChannel() throws SQLException
        {
            PreparedStatement statement = Database.conn
                    .prepareStatement("SELECT `cahchannel` FROM `singleton` ");

            ResultSet rs = statement.executeQuery();
            rs.first();

            String channelID = rs.getString(1);

            rs.close();
            statement.close();

            return channelID;
        }

        public static final void setChannel(String channelID) throws SQLException
        {
            PreparedStatement statement = Database.conn
                    .prepareStatement("UPDATE `singleton` SET `cahchannel` = ?");

            statement.setString(1, channelID);

            statement.executeUpdate();

            statement.close();
        }

        public static final List<String> getDecks() throws SQLException
        {
            PreparedStatement statement = Database.conn
                    .prepareStatement("SELECT `cahdecks` FROM `singleton` ");

            ResultSet rs = statement.executeQuery();
            rs.first();

            String deckString = rs.getString(1);

            rs.close();
            statement.close();

            List<String> decks = new ArrayList<>();
            if (deckString != null)
            {
                StringTokenizer deckTokens = new StringTokenizer(deckString, ":");
                while (deckTokens.hasMoreTokens())
                {
                    decks.add(deckTokens.nextToken());
                }
            }

            return decks;
        }

        public static final void setDecks(List<CahCardDeck> decks) throws SQLException
        {
            PreparedStatement statement = Database.conn
                    .prepareStatement("UPDATE `singleton` SET `cahdecks` = ?");

            StringJoiner deckString = new StringJoiner(":");
            for (CahCardDeck deck : decks)
            {
                deckString.add(deck.getCode());
            }

            statement.setString(1, deckString.toString());

            statement.executeUpdate();

            statement.close();
        }

        public static final String getConfig() throws SQLException
        {
            PreparedStatement statement = Database.conn
                    .prepareStatement("SELECT `cahconfig` FROM `singleton` ");

            ResultSet rs = statement.executeQuery();
            rs.first();

            String config = rs.getString(1);

            rs.close();
            statement.close();

            return config;
        }

        public static final void setConfig(String config) throws SQLException
        {
            PreparedStatement statement = Database.conn
                    .prepareStatement("UPDATE `singleton` SET `cahconfig` = ?");

            statement.setString(1, config);

            statement.executeUpdate();

            statement.close();
        }

    }

}
