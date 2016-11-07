package nuclearcoder.discordbot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nuclearcoder.discordbot.commands.Command;
import nuclearcoder.util.Config;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class EventListener {
	
	private final IDiscordClient client;
	private final String cmdPrefix;
	
	private Map<String, Command> commands;
	
	public EventListener(IDiscordClient client)
	{
		this.client = client;
		this.cmdPrefix = Config.get("command_prefix");
		this.commands = new HashMap<>();
		
		commands.put("test", new Command() {

			@Override
			public void execute(IDiscordClient client, IUser author, IMessage message, String command, String[] args)
			{
				
			}
			
		});
	}
	
	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event)
	{
		IMessage message = event.getMessage();
		IUser author = message.getAuthor();
		String content = message.getContent();
		
		if (content.startsWith(cmdPrefix))
		{
			String pieces[] = content.substring(1).split("\\s+");
			String command = pieces[0];
			
			if (commands.containsKey(command))
			{
				Command executor = commands.get(command);
				String args[] = Arrays.copyOfRange(pieces, 1, pieces.length);
				
				executor.execute(client, author, message, command, args);
			}
		}
	}
	
}
