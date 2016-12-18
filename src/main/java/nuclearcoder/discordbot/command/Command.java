package nuclearcoder.discordbot.command;

import nuclearcoder.discordbot.NuclearBot;
import sx.blah.discord.handle.obj.IMessage;

@FunctionalInterface public interface Command {

    void execute(NuclearBot bot, IMessage message, String command, String args[]);

}
