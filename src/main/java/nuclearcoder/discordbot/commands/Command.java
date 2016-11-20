package nuclearcoder.discordbot.commands;

import nuclearcoder.discordbot.Bot;
import sx.blah.discord.handle.obj.IMessage;

public interface Command {

	public void execute(Bot bot, IMessage message, String command, String args[]) throws Exception;
	
}
