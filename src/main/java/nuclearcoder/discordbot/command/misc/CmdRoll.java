package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Random;

public class CmdRoll implements Command {

    private final Random random = new Random();

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        int bound = 100;
        try
        {
            if (args.length >= 2)
                bound = Integer.parseUnsignedInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            // whatever
        }

        try
        {
            int roll = random.nextInt(bound) + 1;

            IChannel channel = message.getChannel();
            IUser user = message.getAuthor();

            BotUtil.sendMessage(channel, user.mention() + " rolled " + roll + ".");
        }
        catch (IllegalArgumentException e)
        {
            // what. ever.
        }
    }

}
