package nuclearcoder.discordbot.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.vdurmont.emoji.EmojiParser;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.custom.CmdManageCustomCommands;
import nuclearcoder.discordbot.command.manage.CmdManageBot;
import nuclearcoder.discordbot.command.misc.*;
import nuclearcoder.discordbot.database.Database;
import nuclearcoder.discordbot.database.SqlCommands;
import nuclearcoder.discordbot.database.SqlUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CommandManagerImpl implements CommandManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);

    private final NuclearBot bot;
    private final Multimap<String, Command> commands;

    /* temporary fix for ConcurrentModificationException thrown */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public CommandManagerImpl(NuclearBot bot)
    {
        this.bot = bot;
        this.commands = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
    }

    @Override public void initCommands()
    {
        // system commands
        Command manageBot = new CmdManageBot();
        Command manageCustomCommands = new CmdManageCustomCommands(this);
        commands.put("stop", manageBot);
        commands.put("restart", manageBot);
        commands.put("set_op", manageBot);
        commands.put("reload", manageBot);
        commands.put("add_cmd", manageCustomCommands);
        commands.put("rem_cmd", manageCustomCommands);

        // pre-built commands
        commands.put("roll", new CmdRoll());
        commands.put("hug", new CmdHug());
        commands.put("kiss", new CmdKiss());
        commands.put("pat", new CmdPat());
        commands.put("lick", new CmdLick());
        commands.put("uwu", new CmdUwu());
        commands.put("8ball", new Cmd8ball());
        commands.put("ping", new CmdPing());

        // init custom commands
        try
        {
            LOGGER.info("Registering custom commands");

            commands.putAll(SqlCommands.allCommands());
        }
        catch (SQLException e)
        {
            LOGGER.error("SQL error:", e);
        }
    }

    @Override public void clearCommands()
    {
        try
        {
            writeLock.lock();
            Iterator<Command> iterator = commands.values().iterator();
            while (iterator.hasNext())
            {
                iterator.next();
                iterator.remove();
            }
        }
        finally
        {
            writeLock.unlock();
        }
        //commands.clear();
    }

    @Override public void handle(MessageReceivedEvent event)
    {
        IMessage message = event.getMessage();
        String content = EmojiParser.parseFromUnicode(message.getContent(),
                c -> ':' + c.getEmoji().getAliases().get(0) + ':');

        if (content.startsWith(COMMAND_PREFIX))
        {
            String args[] = content.substring(1).split("\\s+");
            String command = args[0];

            if (commands.containsKey(command))
            {
                if (Database.isConnected())
                {
                    /* add the player to the database */
                    try
                    {
                        String userID = message.getAuthor().getID();
                        if (!SqlUsers.isUserPresent(userID))
                            SqlUsers.createUser(userID);
                    }
                    catch (SQLException e)
                    {
                        LOGGER.error("Couldn't init player entry:", e);
                    }
                }

                try
                {
                    readLock.lock();
                    for (Command cmd : commands.get(command))
                    {
                        cmd.execute(bot, message, command, args);
                    }
                }
                finally
                {
                    readLock.unlock();
                }
            }
        }
    }

    @Override public boolean hasCommand(String label)
    {
        return commands.containsKey(label);
    }

    @Override public void putCommand(String label, Command command)
    {
        commands.put(label, command);
    }

    @Override public void removeCommand(String label)
    {
        commands.removeAll(label);
    }

}
