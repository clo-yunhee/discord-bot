package nuclearcoder.discordbot.database;

import nuclearcoder.util.Logger;

public class Database {
	
	public static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
	
	public static final void loadDriver()
	{
		try
		{
			Class.forName(DRIVER).newInstance();
		}
		catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e)
		{
			Logger.error("Could not load DB driver:");
			Logger.printStackTrace(e);
		}
	}
	
}
