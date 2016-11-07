package nuclearcoder.discordbot.commands;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface Command {

	public void execute(IDiscordClient client, IUser author, IMessage message, String command, String args[]);
	
}
