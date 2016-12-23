package nuclearcoder.discordbot;

import nuclearcoder.discordbot.database.Database;
import nuclearcoder.util.Config;
import sx.blah.discord.modules.Configuration;

public class Main {

    public static void main(String args[]) throws Exception
    {
        BotUtil.hackSslVerifier(); // before everything else

        Database.loadDriver(); // load MySQL connector
        Database.openConnection();

        Config.reloadConfig();

        Configuration.LOAD_EXTERNAL_MODULES = false;
        Configuration.AUTOMATICALLY_ENABLE_MODULES = false;

        NuclearBot bot = new NuclearBot(Config.get("token"));
        bot.login();
    }

}
