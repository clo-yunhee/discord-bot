package nuclearcoder.discordbot.command.custom;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import nuclearcoder.discordbot.command.CommandManager;
import nuclearcoder.discordbot.database.SqlCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.sql.SQLException;
import java.util.Arrays;

public class CmdManageCustomCommands implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdManageCustomCommands.class);

    private final CommandManager commandsManager;

    public CmdManageCustomCommands(CommandManager manager)
    {
        this.commandsManager = manager;
    }

    private void register(NuclearBot bot, IMessage message, String command, String answer)
    {
        String guildID = message.getGuild().getID();

        commandsManager.putCommand(command, new CmdCustomCommand(guildID, answer));

        try
        {
            SqlCommands.create(guildID, command, answer);
        }
        catch (SQLException e)
        {
            LOGGER.error("SQL error:", e);
        }
    }

    private void unregister(NuclearBot bot, IMessage message, String command)
    {
        String guildID = message.getGuild().getID();

        commandsManager.removeCommand(command);

        try
        {
            SqlCommands.remove(guildID, command);
        }
        catch (SQLException e)
        {
            LOGGER.error("SQL error:", e);
        }
    }

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        IUser user = message.getAuthor();
        IGuild guild = message.getGuild();

        if (!BotUtil.hasPermission(user, guild, Permissions.MANAGE_MESSAGES))
        {
            BotUtil.reply(message,
                    "you need a guild-wide \"Manage Messages\" permission to (un-)register a command. :warning:");
        }
        else
        {
            String guildID = guild.getID();

            if (command.equalsIgnoreCase("add_cmd"))
            {
                if (args.length <= 2)
                {
                    BotUtil.reply(message, "you must provide command name and reply. :warning:");
                }
                else
                {
                    String argCommand = args[1];

                    if (commandsManager.hasCommand(argCommand))
                    {
                        BotUtil.reply(message,
                                "this command already exists. You can un-register if it's a custom command. :warning:");
                    }
                    else
                    {
                        String argAnswer = String
                                .join(" ", Arrays.copyOfRange(args, 2, args.length));

                        register(bot, message, argCommand, argAnswer);

                        BotUtil.sendMessage(message.getChannel(), "the `" + argCommand
                                + "` command was created successfully. :white_check_mark:");
                    }
                }
            }
            else if (command.equalsIgnoreCase("rem_cmd"))
            {
                if (args.length <= 1)
                {
                    BotUtil.reply(message, "you must provide command name. :warning:");
                }
                else
                {
                    String argCommand = args[1];

                    boolean commandExists = false;
                    try
                    {
                        commandExists = SqlCommands.exists(guildID, argCommand);
                    }
                    catch (SQLException e)
                    {
                        // fail silently
                    }

                    if (!commandExists)
                    {
                        BotUtil.reply(message,
                                "this command does not exist or is reserved. You can register if it's a custom command. :warning:");
                    }
                    else
                    {
                        unregister(bot, message, argCommand);

                        BotUtil.sendMessage(message.getChannel(), "the `" + argCommand
                                + "` command was removed successfully. :white_check_mark:");
                    }
                }
            }
        }
    }

}