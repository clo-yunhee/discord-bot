package nuclearcoder.discordbot.commands;

import nuclearcoder.discordbot.NuclearBot;
import sx.blah.discord.handle.obj.IMessage;

public interface Command {

	public void execute(NuclearBot bot, IMessage message, String command, String args[]) throws Exception;
	
}
