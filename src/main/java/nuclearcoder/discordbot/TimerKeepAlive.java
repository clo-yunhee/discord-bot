package nuclearcoder.discordbot;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimerKeepAlive extends TimerTask {
	
	public final AtomicBoolean alive = new AtomicBoolean(true);
	
	private Timer timer;
	
	public TimerKeepAlive()
	{
		this.timer = new Timer("keepalive", false);
		timer.schedule(this, 300L, 300L);
	}
	
	@Override
	public void run()
	{
		if (alive.get())
		{
			Thread.yield();
		}
		else
		{
			timer.cancel();
		}
	}
	
}
