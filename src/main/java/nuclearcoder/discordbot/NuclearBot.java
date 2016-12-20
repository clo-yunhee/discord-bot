package nuclearcoder.discordbot;

import nuclearcoder.discordbot.cah.card.CahCardProvider;
import nuclearcoder.discordbot.command.CommandManager;
import nuclearcoder.discordbot.database.Database;
import nuclearcoder.discordbot.database.SqlSingletons;
import nuclearcoder.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NuclearBot {

    public static final String CONFIG_FILENAME = "discordbot.cfg";
    public static final Set<String> TRUSTED_HOST_NAMES = new HashSet<>();
    public static final int GET_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
    private static final Logger LOGGER = LoggerFactory.getLogger(NuclearBot.class);
    private final AtomicBoolean reconnect;
    private volatile IDiscordClient client;
    private CommandManager commands;
    private TimerKeepAlive keeper;

    {
        TRUSTED_HOST_NAMES.add(CahCardProvider.HOSTNAME);
        TRUSTED_HOST_NAMES.add("puu.sh");
    }

    public NuclearBot(String token) throws DiscordException
    {
        BotUtil.hackSslVerifier(); // before everything else

        this.client = new ClientBuilder().withToken(token).setDaemon(true)
                .setMaxReconnectAttempts(20).build();
        this.reconnect = new AtomicBoolean(true);
        this.keeper = new TimerKeepAlive();

        client.getDispatcher().registerListener(this);
    }
    
    /* accessors */

    public IDiscordClient getClient()
    {
        return client;
    }

    void login()
    {
        Database.openConnection();

        try
        {
            SqlSingletons.ensureExists();
        }
        catch (SQLException e)
        {
            LOGGER.error("Could not ensure singleton SQL table exists:", e);
        }

        BotUtil.setOperator(Config.get("first_op"), true); // add first op

        LOGGER.info("Logging bot.");
        RequestBuffer.request(() ->
        {
            try
            {
                client.login();
            }
            catch (DiscordException e)
            {
                LOGGER.error("Couldn't log in:", e);
            }
        });
    }

    public void terminate(boolean doReconnect)
    {
        reconnect.set(doReconnect);

        LOGGER.info("Disconnecting bot.");
        try
        {
            client.logout();
        }
        catch (DiscordException e)
        {
            LOGGER.error("Couldn't log out:", e);
        }
    }

    @EventSubscriber public void onReady(ReadyEvent event)
    {
        try
        {
            this.commands = new CommandManager(this);
            client.getDispatcher().registerListener(commands);
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Error while starting command manager:", e);
        }

        LOGGER.info("*** Bot is ready! ***");
    }
    
    /* --- utility --- */

    @EventSubscriber public void onDisconnected(DisconnectedEvent event)
    {
        if (event.getReason() == DisconnectedEvent.Reason.LOGGED_OUT)
        {
            CompletableFuture.runAsync(() ->
            {
                Database.closeConnection();

                if (reconnect.get())
                {
                    reconnect.set(false);

                    client.getDispatcher().unregisterListener(commands);

                    LOGGER.info("Reconnecting bot.");
                    login();
                }
                else
                {
                    keeper.alive.set(false);
                }

            });
        }
    }

}
