package nuclearcoder.discordbot;

import nuclearcoder.discordbot.database.SqlUsers;
import nuclearcoder.util.Logger;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.EnumSet;

public class BotUtil {

    static final void hackSslVerifier()
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
        HttpsURLConnection.setDefaultHostnameVerifier(
                (hostname, session) -> NuclearBot.TRUSTED_HOST_NAMES.contains(hostname));
    }

    public static final boolean hasPermission(IUser user, IGuild guild, Permissions perm)
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

    public static final boolean isOperator(String userID)
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

    public static final void setOperator(String userID, boolean isOp)
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

    public static final void sendMessage(IChannel channel, String content)
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

    public static final void reply(IMessage message, String content)
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

    public static final void sendFileURL(IChannel channel, String content, String url,
            String filename)
    {
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setReadTimeout(NuclearBot.GET_TIMEOUT);
            conn.setConnectTimeout(NuclearBot.GET_TIMEOUT);

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

    public static final String stringFromTime(int time)
    {
        int hrs, min, sec;

        sec = time % 60;
        min = time / 60;
        hrs = min / 60;
        min %= 60;
        hrs %= 24;

        StringBuilder sb = new StringBuilder();

        if (hrs > 0)
        {
            if (hrs < 10)
                sb.append('0');
            sb.append(hrs);
            sb.append(':');
        }

        if (min < 10)
            sb.append('0');
        sb.append(min);
        sb.append(':');

        if (sec < 10)
            sb.append('0');
        sb.append(sec);

        return sb.toString();
    }

    public static int parseTime(String string)
    {
        // that is probably an awful way to parse time

        String[] parts = string.split(":");

        if (parts.length < 2 || parts.length > 3)
            throw new NumberFormatException("Not a time format");

        int i = 0;

        int hrs = parts.length < 3 ? 0 : Integer.parseUnsignedInt(parts[i++]);
        int min = Integer.parseUnsignedInt(parts[i++]);
        int sec = Integer.parseUnsignedInt(parts[i]);

        if (min >= 60 || sec >= 60)
            throw new NumberFormatException("Not a time format");

        return ((hrs * 60) + min) * 60 + sec;
    }

}




















