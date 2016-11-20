package nuclearcoder.discordbot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import nuclearcoder.discordbot.commands.CmdManageBot;
import nuclearcoder.discordbot.commands.CmdManageSimpleCommands;
import nuclearcoder.discordbot.commands.Command;
import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class CommandManager implements IListener<MessageReceivedEvent> {
	
	private final Bot bot;
	private final String cmdPrefix;
	
	private final Multimap<String, Command> commands;
	
	public CommandManager(Bot bot)
	{
		this.bot = bot;
		this.cmdPrefix = Config.get("command_prefix");
		this.commands = HashMultimap.create(30, 2);
		
		initCommands();
	}
	
	private void initCommands()
	{
		CmdManageBot manageBot = new CmdManageBot();
		commands.put("stop", manageBot);
		commands.put("restart", manageBot);
		commands.put("set_op", manageBot);
		
		CmdManageSimpleCommands manageSimpleCommand = new CmdManageSimpleCommands(this, bot.getDatabase());
		commands.put(CmdManageSimpleCommands.ADD_CMD, manageSimpleCommand);
		commands.put(CmdManageSimpleCommands.REM_CMD, manageSimpleCommand);
	}
	
	@Override
	public void handle(MessageReceivedEvent event)
	{
		IMessage message = event.getMessage();
		String content = message.getContent();
		
		if (content.startsWith(cmdPrefix))
		{
			String args[] = content.substring(1).split("\\s+");
			String command = args[0];
			
			if (commands.containsKey(command))
			{
				for (Command executor : commands.get(command))
				{
					try
					{
						executor.execute(bot, message, command, args);
					}
					catch (Exception e)
					{
						Logger.warning("Exception while executing command:");
						Logger.printStackTrace(e);
					}
				}
			}
		}
	}
	
	public boolean hasCommand(String label)
	{
		return commands.containsKey(label);
	}
	
	public void putCommand(String label, Command command)
	{
		commands.put(label, command);
	}
	
	public void removeCommand(String label)
	{
		commands.removeAll(label);
	}
	
}
