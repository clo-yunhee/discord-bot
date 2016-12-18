package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IMessage;

public class CmdUwu implements Command {

    private static final String UWU = "https://puu.sh/sAQuH/29cca0f3cd.jpg";

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        bot.sendFileURL(message.getChannel(), "", UWU, "uwu.jpg");
    }

}
