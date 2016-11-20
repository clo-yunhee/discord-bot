package nuclearcoder.discordbot;

import nuclearcoder.discordbot.database.Database;
import nuclearcoder.util.Config;
import sx.blah.discord.util.DiscordException;

public class Main {

	public static void main(String args[]) throws DiscordException
	{
		Database.loadDriver();
		
		Bot bot = new Bot(Config.get("token"));
		bot.login();
		
		while (bot.reconnect())
		{
			Thread.yield();
		}
	}

}
