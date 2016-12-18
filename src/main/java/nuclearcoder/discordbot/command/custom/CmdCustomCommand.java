package nuclearcoder.discordbot.command.custom;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.text.MessageFormat;

public class CmdCustomCommand implements Command {

    private String guildID;
    private MessageFormat formatter;

    public CmdCustomCommand(String guildID, String format)
    {
        this.guildID = guildID;
        this.formatter = new MessageFormat(format.replace("%", "%%").replaceAll("\n", "%n"));
        // don't worry about Locale, we're only formatting strings
    }

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        if (guildID.equals(message.getGuild().getID()))
        {
            IUser author = message.getAuthor();

            Object[] cmdArgs = new Object[args.length];
            cmdArgs[0] = author.mention();
            if (args.length > 0)
            {
                System.arraycopy(args, 1, cmdArgs, 1, args.length - 1);
            }

            String answer = formatter.format(cmdArgs);
            if (answer != null)
            {
                bot.sendMessage(message.getChannel(), answer);
            }
        }
    }

}
