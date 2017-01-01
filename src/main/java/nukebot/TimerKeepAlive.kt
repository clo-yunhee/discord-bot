package nukebot

import nukebot.database.Database
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class TimerKeepAlive : TimerTask() {

    internal val alive = AtomicBoolean(true)

    private val timer = Timer("keep-alive", false)

    init {
        timer.schedule(this, KEEP_ALIVE, KEEP_ALIVE)
    }

    override fun run() {
        if (alive.get()) {
            Database.keepAlive()
        } else {
            timer.cancel()
        }
    }

}
