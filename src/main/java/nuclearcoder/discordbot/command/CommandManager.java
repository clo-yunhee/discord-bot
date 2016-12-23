package nuclearcoder.discordbot.command;

import nuclearcoder.util.Config;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public interface CommandManager extends IListener<MessageReceivedEvent> {

    String COMMAND_PREFIX = Config.get("command_prefix");

    void initCommands();

    void clearCommands();

    void handle(MessageReceivedEvent event);

    Command getCommand(String label);

    void putCommand(String label, Command command);

    void removeCommand(String label);

}