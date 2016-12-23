package nuclearcoder.discordbot.database;

import nuclearcoder.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static final String DRIVER = "com.mysql.jdbc.Driver";

    public static Connection conn = null;

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
            LOGGER.error("Could not load DB driver:", e);
        }
    }

    public static final void openConnection()
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
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema,
                    connectionProps);
        }
        catch (SQLException e)
        {
            LOGGER.error("Could not open DB connection:", e);
        }
    }

    public static final void closeConnection()
    {
        try
        {
            conn.close();
        }
        catch (SQLException e)
        {
            LOGGER.error("Could not close DB connection:", e);
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
            LOGGER.error("Could not query DB state:", e);
            return false;
        }
    }

    public static final void keepAlive()
    {
        try (Statement statement = conn.createStatement())
        {
            statement.execute("SELECT null FROM singleton");
        }
        catch (SQLException e)
        {
            LOGGER.error("Could not keep alive:", e);
        }
    }
}
