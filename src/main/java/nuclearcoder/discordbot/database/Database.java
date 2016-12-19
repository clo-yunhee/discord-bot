package nuclearcoder.discordbot.database;

import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {

    private static final String DRIVER = "com.mysql.jdbc.Driver";

    static Connection conn = null;

    public static final void loadDriver()
    {
        try
        {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName(DRIVER).newInstance();
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
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
        connectionProps.put("useSSL", "false");
        connectionProps.put("autoReconnect", "true");

        boolean success = true;

        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema,
                    connectionProps);
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

    public static final boolean isConnected()
    {
        try
        {
            return !conn.isClosed();
        }
        catch (SQLException e)
        {
            Logger.error("Could not query DB state:");
            Logger.printStackTrace(e);
            return false;
        }
    }

    public static final void keepAlive()
    {
        try (Statement statement = conn.createStatement())
        {
            statement.execute("SELECT null FROM singletons");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
