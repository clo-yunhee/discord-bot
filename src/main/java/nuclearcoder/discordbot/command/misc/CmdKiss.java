package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;

public class CmdKiss implements Command {

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        if (args.length >= 2)
        {
            final IChannel channel = message.getChannel();
            final IUser user = message.getAuthor();

            final String target = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();

            BotUtil.sendMessage(channel,
                    user.mention() + " kisses " + target + "  ( \u02D8 \u00B3\u02D8) ~~");
        }
    }

}
