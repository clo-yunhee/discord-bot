package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IMessage;

public class CmdPing implements Command {

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        bot.reply(message, "pong!");
    }

}
