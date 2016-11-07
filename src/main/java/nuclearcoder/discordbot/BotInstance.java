package nuclearcoder.discordbot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import nuclearcoder.util.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class BotInstance {
	
	private volatile IDiscordClient client;
	private final AtomicBoolean reconnect;
	
	private EventListener listener;
	
	public BotInstance(String token) throws DiscordException
	{
		this.client = new ClientBuilder().withToken(token).build();
		this.listener = new EventListener(client);
		this.reconnect = new AtomicBoolean(true);
	}
	
	public void login() throws DiscordException
	{
		client.login();
		client.getDispatcher().registerListener(this);
		client.getDispatcher().registerListener(listener);
	}
	
	public void terminate()
	{
		reconnect.set(false);
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
        Logger.info("*** Bot is ready ***");
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent event)
    {
        CompletableFuture.runAsync(() -> {
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

}
