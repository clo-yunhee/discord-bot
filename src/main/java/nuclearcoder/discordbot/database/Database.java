package nuclearcoder.discordbot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;

public class Database {
	
	public static final String DRIVER = "com.mysql.jdbc.Driver";
	
	static Connection conn = null;
	
	public static final void loadDriver()
	{
		try
		{
            // The newInstance() call is a work around for some
            // broken Java implementations
			Class.forName(DRIVER).newInstance();
		}
		catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e)
		{
            // handle the error
			Logger.error("Could not load DB driver:");
			Logger.printStackTrace(e);
		}
	}
	
	public static final boolean openConnection()
	{
		String host = Config.get("db_host");
		String port = Config.get("db_port");
		
		String user = Config.get("db_user");
		String pass = Config.get("db_pass");
		
		String schema = Config.get("db_schema");
		
		Properties connectionProps = new Properties();
	    connectionProps.put("user", user);
	    connectionProps.put("password", pass);

	    boolean success = true;
	    
        try
        {
			conn = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":" + port + "/",
						connectionProps);
			conn.setSchema(schema);
			SQLTables.initTables();
		}
        catch (SQLException e)
        {
			Logger.error("Could not open DB connection:");
			Logger.printStackTrace(e);
			
			success = false;
		}
        
        return success;
	}
	
	public static final void closeConnection()
	{
		try
	    {
			conn.close();
		}
	    catch (SQLException e)
	    {
			Logger.error("Could not close DB connection:");
			Logger.printStackTrace(e);
		}
	}
	
	public static final Connection getConnection()
	{
		return conn;
	}
	
}
