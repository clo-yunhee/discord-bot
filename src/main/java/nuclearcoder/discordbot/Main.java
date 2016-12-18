package nuclearcoder.discordbot;

import nuclearcoder.discordbot.database.Database;
import nuclearcoder.util.Config;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class Main {

    public static void main(String args[]) throws RateLimitException, DiscordException
    {
        Database.loadDriver(); // load MySQL connector

        NuclearBot bot = new NuclearBot(Config.get("token"));
        bot.login();
    }

}
