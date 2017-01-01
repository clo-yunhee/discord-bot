package nukebot

import nukebot.command.CommandManager
import nukebot.command.CommandManagerImpl
import nukebot.database.SqlSingletons
import nukeutils.Config
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.DisconnectedEvent
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.modules.ModuleLoader
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.RequestBuffer
import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean


class NuclearBot(token: String) {

    private val keeper = TimerKeepAlive()
    private val reconnect = AtomicBoolean(false)

    val client: IDiscordClient = ClientBuilder().withToken(token).setMaxReconnectAttempts(Int.MAX_VALUE).build()

    val commands: CommandManager = CommandManagerImpl(this)
    val moduleLoader: ModuleLoader = client.moduleLoader

    init {
        client.dispatcher.registerListener(this)
    }

    fun login() {
        try {
            SqlSingletons.ensureExists()
        } catch (e: SQLException) {
            LOGGER.error("Could not ensure that singleton SQL table exists", e)
        }

        setOperator(Config["first_op"], true) // add first op

        LOGGER.info("Logging bot...")

        RequestBuffer.request {
            try {
                client.login()
            } catch (e: DiscordException) {
                LOGGER.error("Could not log in", e)
            }
        }
    }

    fun terminate(doReconnect: Boolean) {
        reconnect.set(doReconnect)

        LOGGER.info("Disconnecting bot...")
        try {
            client.logout()
        } catch (e: DiscordException) {
            LOGGER.error("Could not log out", e)
        }
    }

    @EventSubscriber
    fun onReady(event: ReadyEvent) {
        try {
            commands.initCommands()
            client.dispatcher.registerListener(commands)
        } catch (e: RuntimeException) {
            LOGGER.error("Could not start command manager", e)
        }

        reloadModules(moduleLoader)

        LOGGER.info("*** Bot is ready! ***")
    }

    @EventSubscriber
    fun onDisconnected(event: DisconnectedEvent) {
        commands.clearCommands()

        if (event.reason == DisconnectedEvent.Reason.LOGGED_OUT) {
            CompletableFuture.runAsync {
                if (reconnect.compareAndSet(true, false)) {
                    client.dispatcher.unregisterListener(commands)

                    LOGGER.info("Reconnecting bot...")

                    login()
                } else {
                    keeper.alive.set(false)
                }
            }
        }
    }

}
