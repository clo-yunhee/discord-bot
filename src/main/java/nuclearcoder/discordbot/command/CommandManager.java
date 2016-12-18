package nuclearcoder.discordbot.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiParser.EmojiTransformer;
import com.vdurmont.emoji.EmojiParser.UnicodeCandidate;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.custom.CmdManageCustomCommands;
import nuclearcoder.discordbot.command.manage.CmdManageBot;
import nuclearcoder.discordbot.command.misc.*;
import nuclearcoder.discordbot.command.modules.CmdCah;
import nuclearcoder.discordbot.command.modules.CmdMusic;
import nuclearcoder.discordbot.database.Database;
import nuclearcoder.discordbot.database.SqlCommands;
import nuclearcoder.discordbot.database.SqlUsers;
import nuclearcoder.util.Config;
import nuclearcoder.util.Logger;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.SQLException;

public class CommandManager implements IListener<MessageReceivedEvent> {

    private static final EmojiTransformer EMOJI_TRANSFORMER = new CustomEmojiTransformer();
    private final NuclearBot bot;
    private final String cmdPrefix;
    private final Multimap<String, Command> commands;

    public CommandManager(NuclearBot bot)
    {
        this.bot = bot;
        this.cmdPrefix = Config.get("command_prefix");
        this.commands = ArrayListMultimap.create(50, 2);

        initCommands();
    }

    private void initCommands()
    {
        IDiscordClient client = bot.getClient();

        // system commands
        Command manageBot = new CmdManageBot();
        Command manageCustomCommands = new CmdManageCustomCommands(this);
        commands.put("stop", manageBot);
        commands.put("restart", manageBot);
        commands.put("set_op", manageBot);
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

        // modules
        commands.put("music", new CmdMusic());
        commands.put("cah", new CmdCah(client));

        // init custom commands
        try
        {
            Logger.info("Registering custom commands");

            commands.putAll(SqlCommands.allCommands());
        }
        catch (SQLException e)
        {
            Logger.error("SQL error:");
            Logger.printStackTrace(e);
        }
    }

    @Override public void handle(MessageReceivedEvent event)
    {
        IMessage message = event.getMessage();
        String content = EmojiParser.parseFromUnicode(message.getContent(), EMOJI_TRANSFORMER);

        if (content.startsWith(cmdPrefix))
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
                        Logger.error("Couldn't init player entry:");
                        Logger.printStackTrace(e);
                    }
                }

                for (Command executor : commands.get(command))
                {
                    executor.execute(bot, message, command, args);
                }
            }
        }

    }

    public boolean hasCommand(String label)
    {
        return commands.containsKey(label);
    }

    public void putCommand(String label, Command command)
    {
        commands.put(label, command);
    }

    public void removeCommand(String label)
    {
        commands.removeAll(label);
    }

    private static class CustomEmojiTransformer implements EmojiTransformer {

        @Override public String transform(UnicodeCandidate unicodeCandidate)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(':');
            sb.append(unicodeCandidate.getEmoji().getAliases().get(0));
            sb.append(':');
            return sb.toString();
        }

    }

}
