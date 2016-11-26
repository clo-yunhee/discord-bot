package nuclearcoder.discordbot;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import nuclearcoder.discordbot.commands.CommandManager;
import nuclearcoder.discordbot.database.Database;
import nuclearcoder.util.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class NuclearBot {
	
	private volatile IDiscordClient client;
	private final AtomicBoolean reconnect;

	private CommandManager commands;
	
	private TimerKeepAlive keeper;
	
	public NuclearBot(String token) throws DiscordException, RateLimitException
	{
		this.client = new ClientBuilder().withToken(token)
											.setDaemon(true)
											.build();
		this.reconnect = new AtomicBoolean(true);
		this.keeper = new TimerKeepAlive();
		
		client.getDispatcher().registerListener(this);
		
		//operators.add(Config.get("first_op")); // add first op
	}
	
	/* accessors */
	
	public IDiscordClient getClient()
	{
		return client;
	}
	
	public CommandManager getCommandsManager()
	{
		return commands;
	}
	
	public boolean reconnect()
	{
		return reconnect.get();
	}
	
	/* bot methods */
	
	public void login() throws RateLimitException, DiscordException 
	{
		Database.openConnection();

		Logger.info("Logging bot");
		client.login();
	}
	
	public void terminate(boolean doReconnect) throws DiscordException
	{
		reconnect.set(doReconnect);
		
		Logger.info("Disconnecting bot");
        client.logout();
    }
	
	@EventSubscriber
    public void onReady(ReadyEvent event)
	{
        Logger.info("*** Bot is ready ***");
        
		this.commands = new CommandManager(this);
		client.getDispatcher().registerListener(commands);
    }

    @EventSubscriber
    public void onDisconnected(DisconnectedEvent event)
    {
    	CompletableFuture.runAsync(() -> {
    		
	        Database.closeConnection();
	        
	        if (reconnect.get())
	        {
	        	reconnect.set(false);
	        	
	        	client.getDispatcher().unregisterListener(commands);
	
	            Logger.info("Reconnecting bot");
	            try
	            {
	                login();
	            }
	            catch (RateLimitException | DiscordException e)
	            {
	                Logger.warning("Failed to reconnect bot:");
	                Logger.printStackTrace(e);
	            }
	        }
	        else
	        {
	        	keeper.alive.set(false);
	        }
	        
    	});
    }
    
    /* --- utility --- */
    
    public boolean hasPermission(IUser user, IGuild guild, Permissions perm)
    {
    	for (IRole role : user.getRolesForGuild(guild))
		{
    		EnumSet<Permissions> perms = role.getPermissions();
			if (perms.contains(Permissions.ADMINISTRATOR) || perms.contains(perm))
			{
				return true;
			}
		}
    	return false;
    }
    
    public boolean isOperator(IUser user)
    {
    	return true;//operators.contains(user.getID());
    }
    
	public void setOperator(IUser user, boolean isOp)
	{
		String userId = user.getID();
		if (isOp)
		{
			//operators.add(userId);
		}
		else
		{
			//operators.remove(userId);
		}
	}

}
