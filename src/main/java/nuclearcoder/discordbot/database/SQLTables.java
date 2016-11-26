package nuclearcoder.discordbot.database;

import java.sql.SQLException;

public class SQLTables {
	
	public static final void initTables() throws SQLException
	{
		Database.conn.prepareStatement("CREATE TABLE users ("
				+ "uid varchar(18)"
				+ ");");
	}
	
}
