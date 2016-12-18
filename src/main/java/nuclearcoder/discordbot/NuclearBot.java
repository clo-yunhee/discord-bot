package nuclearcoder.discordbot;

import nuclearcoder.discordbot.cah.card.CahCardProvider;
import nuclearcoder.discordbot.command.CommandManager;
import nuclearcoder.discordbot.database.Database;
import nuclearcoder.discordbot.database.SqlSingletons;
import nuclearcoder.discordbot.database.SqlUsers;
import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NuclearBot {

    public static final int GET_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
    public static final Set<String> TRUSTED_HOSTNAMES = new HashSet<>();
    public static final String CONFIG_FILENAME = "discordbot.cfg";
    public static final String LOG_FILENAME = "discordbot.log";
    private final AtomicBoolean reconnect;
    private volatile IDiscordClient client;
    private CommandManager commands;
    private TimerKeepAlive keeper;

    {
        TRUSTED_HOSTNAMES.add(CahCardProvider.HOSTNAME);
        TRUSTED_HOSTNAMES.add("puu.sh");
    }

    public NuclearBot(String token) throws DiscordException
    {
        hackSslVerifier(); // before everything else

        this.client = new ClientBuilder().withToken(token).setDaemon(true)
                .setMaxReconnectAttempts(20).build();
        this.reconnect = new AtomicBoolean(true);
        this.keeper = new TimerKeepAlive();

        client.getDispatcher().registerListener(this);
    }
    
    /* accessors */

    public static final void hackSslVerifier()
    {
        // FIXME: My JVM doesn't like the certificate. I should go add
        // StartSSL's root certificate to
        // its trust store, and document steps. For now, I'm going to disable
        // SSL certificate checking.

        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            @Override public void checkClientTrusted(final X509Certificate[] certs,
                    final String authType)
            {
            }

            @Override public void checkServerTrusted(final X509Certificate[] certs,
                    final String authType)
            {
            }
        } };

        try
        {
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (final Exception e)
        {
            Logger.error("Unable to install trust-all security manager:");
            Logger.printStackTrace(e);
        }

        // Create host name verifier that only trusts cardcast
        final HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override public boolean verify(final String hostname, final SSLSession session)
            {
                return TRUSTED_HOSTNAMES.contains(hostname);
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public IDiscordClient getClient()
    {
        return client;
    }

    public void login()
    {
        Database.openConnection();

        try
        {
            SqlSingletons.ensureExists();
        }
        catch (SQLException e)
        {
            Logger.error("Could not ensure singleton SQL table exists:");
            Logger.printStackTrace(e);
        }

        setOperator(Config.get("first_op"), true); // add first op

        Logger.info("Logging bot.");
        RequestBuffer.request(() ->
        {
            try
            {
                client.login();
            }
            catch (DiscordException e)
            {
                Logger.error("Couldn't log in:");
                Logger.printStackTrace(e);
            }
        });
    }

    public void terminate(boolean doReconnect)
    {
        reconnect.set(doReconnect);

        Logger.info("Disconnecting bot.");
        try
        {
            client.logout();
        }
        catch (DiscordException e)
        {
            Logger.error("Couldn't log out:");
            Logger.printStackTrace(e);
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
            Logger.error("Error while starting command manager:");
            Logger.printStackTrace(e);
        }

        Logger.info("*** Bot is ready! ***");
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

                    Logger.info("Reconnecting bot.");
                    login();
                }
                else
                {
                    keeper.alive.set(false);
                }

            });
        }
    }

    public boolean hasPermission(IUser user, IGuild guild, Permissions perm)
    {
        for (IRole role : user.getRolesForGuild(guild))
        {
            EnumSet<Permissions> perms = role.getPermissions();
            if (perms.contains(Permissions.ADMINISTRATOR) || perms.contains(perm))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isOperator(String userID)
    {
        try
        {
            return SqlUsers.isOperator(userID);
        }
        catch (SQLException e)
        {
            Logger.error("SQL error:");
            Logger.printStackTrace(e);

            return false;
        }
    }

    public void setOperator(String userID, boolean isOp)
    {
        try
        {
            SqlUsers.setOperator(userID, isOp ? 1 : 0);
        }
        catch (SQLException e)
        {
            Logger.error("SQL error:");
            Logger.printStackTrace(e);
        }
    }

    public void sendMessage(IChannel channel, String content)
    {
        RequestBuffer.request(() ->
        {
            try
            {
                channel.sendMessage(content);
            }
            catch (MissingPermissionsException | DiscordException e)
            {
                Logger.error("Couldn't send message:");
                Logger.printStackTrace(e);
            }
        });
    }

    public void reply(IMessage message, String content)
    {
        RequestBuffer.request(() ->
        {
            try
            {
                message.reply(content);
            }
            catch (MissingPermissionsException | DiscordException e)
            {
                Logger.error("Couldn't send message:");
                Logger.printStackTrace(e);
            }
        });
    }

    public void sendFileURL(IChannel channel, String content, String url, String filename)
    {
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setReadTimeout(GET_TIMEOUT);
            conn.setConnectTimeout(GET_TIMEOUT);

            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            try (InputStream in = conn.getInputStream())
            {
                RequestBuffer.request(() ->
                {
                    try
                    {
                        channel.sendFile(content, false, in, filename);
                    }
                    catch (MissingPermissionsException | DiscordException e)
                    {
                        Logger.error("Couldn't send file:");
                        Logger.printStackTrace(e);
                    }
                });
            }
        }
        catch (IOException e)
        {
            Logger.error("Couldn't send file:");
            Logger.printStackTrace(e);
        }
    }

}
