package nuclearcoder.discordbot.commands;

import nuclearcoder.discordbot.NuclearBot;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class CmdManageSimpleCommands implements Command {

	private final CommandManager commandsManager;
	
	public CmdManageSimpleCommands(CommandManager manager)
	{
		this.commandsManager = manager;
	}
	
	private void register(NuclearBot bot, IMessage message, String command, String answer)
	{
		String guildId = message.getGuild().getID();
		
		commandsManager.putCommand(command, new CmdSimpleCommand(guildId, answer));
		//simpleCommands.get(guildId).add(command);
	}
	
	private void unregister(NuclearBot bot, IMessage message, String command)
	{
		String guildId = message.getGuild().getID();
		
		commandsManager.removeCommand(command);
		//simpleCommands.get(guildId).remove(command);
	}

	@Override
	public void execute(NuclearBot bot, IMessage message, String command, String[] args) throws MissingPermissionsException, RateLimitException, DiscordException
	{
		IUser user = message.getAuthor();
		IGuild guild = message.getGuild();
		
		if (!bot.hasPermission(user, guild, Permissions.MANAGE_MESSAGES))
		{
			message.reply("you need a guild-wide \"Manage Messages\" permission to (un-)register a command. :warning:");
		}
		else
		{
			String guildId = guild.getID();
			
			/*if (!simpleCommands.containsKey(guildId))
			{
				simpleCommands.put(guildId, new HashSet<String>());
			}
			Set<String> guildSet = simpleCommands.get(guildId);*/
			
			if (command.equalsIgnoreCase("add_cmd"))
			{
				if (args.length <= 2)
				{
					message.reply("you must provide command name and reply. :warning:");
				}
				else
				{
					String argCommand = args[1];
					
					/*if (commandsManager.hasCommand(argCommand))
					{
						message.reply("this command already exists. You can unregister if it's a custom command. :warning:");
					}
					else
					{
						String argAnswer = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
						
						register(bot, message, argCommand, argAnswer);
						
						message.getChannel().sendMessage("the `" + argCommand + "` command was created succesfully. :white_check_mark:");
					}*/
				}
			}
			else if (command.equalsIgnoreCase("rem_cmd"))
			{
				if (args.length <= 1)
				{
					message.reply("you must provide command name. :warning:");
				}
				else
				{
					String argCommand = args[1];
					
					/*if (!guildSet.contains(argCommand))
					{
						message.reply("this command does not exist or is reserved. You can register if it's a custom command. :warning:");
					}
					else
					{
						unregister(bot, message, argCommand);
	
						message.getChannel().sendMessage("the `" + argCommand + "` command was removed succesfully. :white_check_mark:");
					}*/
				}
			}
		}
	}
	
}