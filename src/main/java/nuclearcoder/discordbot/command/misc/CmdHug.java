package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;

public class CmdHug implements Command {

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        if (args.length >= 2)
        {
            final IChannel channel = message.getChannel();
            final IUser user = message.getAuthor();

            final String target = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();

            bot.sendMessage(channel,
                    user.mention() + " hugs " + target + "  (\u3064\u2267\u25BD\u2266)\u3064");
        }
    }

}
