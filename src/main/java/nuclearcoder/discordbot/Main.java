package nuclearcoder.discordbot;

import nuclearcoder.util.Config;
import sx.blah.discord.util.DiscordException;

public class Main {

	public static void main(String args[]) throws DiscordException
	{
		BotInstance bot = new BotInstance(Config.get("token"));
		bot.login();
	}

}
