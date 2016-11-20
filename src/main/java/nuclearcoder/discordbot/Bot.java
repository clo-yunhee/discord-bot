package nuclearcoder.discordbot;

import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;
import nuclearcoder.util.UtilConstants;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class Bot {
	
	private volatile IDiscordClient client;
	private final AtomicBoolean reconnect;
	
	private DB database;

	private CommandManager commands;
	
	private NavigableSet<String> operators;
	
	public Bot(String token) throws DiscordException
	{
		openDatabase();
		
		this.client = new ClientBuilder().withToken(token).build();
		this.reconnect = new AtomicBoolean(true);
		
		this.operators = database.treeSet("operators", Serializer.STRING).createOrOpen();
		
		operators.add(Config.get("first_op")); // add first op
	}
	
	private void openDatabase()
	{
		database = DBMaker.fileDB(UtilConstants.DB_FILENAME)
							.checksumHeaderBypass()
							.closeOnJvmShutdown()
							.transactionEnable()
							.make();
	}
	
	/* accessors */
	
	public IDiscordClient getClient()
	{
		return client;
	}
	
	public DB getDatabase()
	{
		return database;
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
	
	public void login() throws DiscordException
	{
		if (database.isClosed())
		{
			openDatabase();
		}
		
		client.login();
		client.getDispatcher().registerListener(this);
	}
	
	public void terminate(boolean terminate)
	{
		reconnect.set(terminate);
        try
        {
            client.logout();
        }
        catch (RateLimitException | DiscordException e)
        {
            Logger.warning("Logout failed:");
            Logger.printStackTrace(e);
        }
    }
	
	@EventSubscriber
    public void onReady(ReadyEvent event)
	{
		this.commands = new CommandManager(this);
		client.getDispatcher().registerListener(commands);
		
        Logger.info("*** Bot is ready ***");
    }

    @EventSubscriber
    public void onDiscordDisconnected(DiscordDisconnectedEvent event)
    {
    	CompletableFuture.runAsync(() -> {
    		client.getDispatcher().unregisterListener(commands);
    		client.getDispatcher().unregisterListener(this);
    		
            Logger.info("Saving database");
            database.commit();
            database.close();
            
	        if (reconnect.get())
	        {
	            Logger.info("Reconnecting bot");
	            try
	            {
	                login();
	            }
	            catch (DiscordException e)
	            {
	                Logger.warning("Failed to reconnect bot:");
	                Logger.printStackTrace(e);
	            }
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
    	return operators.contains(user.getID());
    }
    
	public void setOperator(IUser user, boolean isOp)
	{
		String userId = user.getID();
		if (isOp)
		{
			operators.add(userId);
		}
		else
		{
			operators.remove(userId);
		}
	}

}
