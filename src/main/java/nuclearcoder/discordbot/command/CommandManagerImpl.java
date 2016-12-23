package nuclearcoder.discordbot.command;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommandManagerImpl implements CommandManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);

    private final NuclearBot bot;
    private final Map<String, Command> commands;

    public CommandManagerImpl(NuclearBot bot)
    {
        this.bot = bot;
        this.commands = new HashMap<>();
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

            SqlCommands.allCommands(commands);
        }
        catch (SQLException e)
        {
            LOGGER.error("SQL error:", e);
        }
    }

    @Override public void clearCommands()
    {
        Iterator<Command> iterator = commands.values().iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            iterator.remove();
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
            String name = args[0];

            Command command = commands.get(name);

            if (command != null)
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

                command.execute(bot, message, name, args);
            }
        }
    }

    @Override public Command getCommand(String label)
    {
        return commands.get(label);
    }

    @Override public void putCommand(String label, Command command)
    {
        commands.put(label, command);
    }

    @Override public void removeCommand(String label)
    {
        commands.remove(label);
    }

}
