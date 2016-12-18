package nuclearcoder.discordbot;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimerKeepAlive extends TimerTask {

    public static final long KEEP_ALIVE = TimeUnit.SECONDS.toMillis(2);

    public final AtomicBoolean alive = new AtomicBoolean(true);

    private Timer timer;

    public TimerKeepAlive()
    {
        this.timer = new Timer("keepalive", false);
        timer.schedule(this, KEEP_ALIVE, KEEP_ALIVE);
    }

    @Override public void run()
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
