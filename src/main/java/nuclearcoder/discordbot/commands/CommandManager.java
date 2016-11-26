package nuclearcoder.discordbot.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class CommandManager implements IListener<MessageReceivedEvent> {
	
	private final NuclearBot bot;
	private final String cmdPrefix;
	
	private final Map<String, LinkedList<Command>> commands;
	
	public CommandManager(NuclearBot bot)
	{
		this.bot = bot;
		this.cmdPrefix = Config.get("command_prefix");
		this.commands = new HashMap<>();
		
		initCommands();
	}
	
	private void initCommands()
	{
		Set<Command> manageBot = Collections.singleton(new CmdManageBot());
		commands.put("stop", new LinkedList<Command>(manageBot));
		commands.put("restart", new LinkedList<Command>(manageBot));
		commands.put("set_op", new LinkedList<Command>(manageBot));
		
		Set<Command> manageSimpleCommands = Collections.singleton(new CmdManageSimpleCommands(this));
		commands.put("add_cmd", new LinkedList<Command>(manageSimpleCommands));
		commands.put("rem_cmd", new LinkedList<Command>(manageSimpleCommands));
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
		if (!commands.containsKey(label))
		{
			commands.put(label, new LinkedList<Command>());
		}
		commands.get(label).add(command);
	}
	
	public void removeCommand(String label)
	{
		commands.remove(label);
	}
	
}
