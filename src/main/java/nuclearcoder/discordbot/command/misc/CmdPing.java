package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IMessage;

public class CmdPing implements Command {

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        BotUtil.reply(message, "pong!");
    }

}
