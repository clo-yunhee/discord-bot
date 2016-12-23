package nuclearcoder.discordbot.command.custom;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import nuclearcoder.util.ArgumentFormatter;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CmdCustomCommand implements Command {

    // used when there is only one format
    private String singleGuildID;
    private ArgumentFormatter singleFormat;

    // used when there are 2+ formats
    private Map<String, ArgumentFormatter> formats;

    public CmdCustomCommand(String guildID, String format)
    {
        this.singleGuildID = guildID;
        this.singleFormat = buildFormatter(format);

        this.formats = null;
    }

    private ArgumentFormatter buildFormatter(String format)
    {
        return new ArgumentFormatter(format);
    }

    public void put(String guildID, String format)
    {
        if (formats == null)
        {
            formats = new HashMap<>();
            formats.put(singleGuildID, singleFormat);

            singleGuildID = null;
            singleFormat = null;
        }

        formats.put(guildID, buildFormatter(format));
    }

    // returns true if empty after removal
    public boolean remove(String guildID)
    {
        boolean isEmpty = (formats == null);

        if (isEmpty)
        {
            singleGuildID = null;
            singleFormat = null;
        }
        else
        {
            formats.remove(guildID);
            if (formats.isEmpty())
            {
                formats = null;
            }
        }

        return isEmpty;
    }

    private ArgumentFormatter getFormatter(String guildID)
    {
        if (formats == null)
        {
            return guildID.equals(singleGuildID) ? singleFormat : null;
        }
        else
        {
            return formats.get(guildID);
        }
    }

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        String guildID = message.getGuild().getID();
        ArgumentFormatter formatter = getFormatter(guildID);

        if (formatter != null)
        {
            IUser author = message.getAuthor();

            String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
            String formattedMessage = formatter.format(author.mention(), cmdArgs);
            if (formattedMessage != null)
            {
                BotUtil.sendMessage(message.getChannel(), formattedMessage);
            }
        }
    }

}
