package nuclearcoder.discordbot.commands;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.util.ArgumentFormatter;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class CmdSimpleCommand implements Command {
	
	private String guildId;
	private ArgumentFormatter formatter;
	
	public CmdSimpleCommand(String guildId, String format)
	{
		this.guildId = guildId;
		this.formatter = new ArgumentFormatter(format);
	}

	@Override
	public void execute(NuclearBot bot, IMessage message, String command, String[] args) throws MissingPermissionsException, RateLimitException, DiscordException
	{
		if (guildId.equals(message.getGuild().getID()))
		{
			IUser author = message.getAuthor();
			
			String answer = formatter.format(author.mention(), args);
			if (answer != null)
			{
				message.getChannel().sendMessage(answer);
			}
		}
	}

}
