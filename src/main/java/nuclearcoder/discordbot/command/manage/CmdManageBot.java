package nuclearcoder.discordbot.command.manage;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class CmdManageBot implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdManageBot.class);

    @Override public void execute(final NuclearBot bot, final IMessage message,
            final String command, final String[] args)
    {
        final IUser user = message.getAuthor();

        if (BotUtil.isOperator(user.getID()))
        {
            RequestBuffer.request(() ->
            {
                try
                {
                    IPrivateChannel pmChannel = user.getOrCreatePMChannel();

                    if (command.equalsIgnoreCase("stop"))
                    {
                        bot.terminate(false);
                    }
                    else if (command.equalsIgnoreCase("restart"))
                    {
                        bot.terminate(true);
                    }
                    else if (command.equalsIgnoreCase("set_op"))
                    {
                        if (args.length > 2)
                        {
                            boolean setOp = Boolean.parseBoolean(args[1]);
                            String notification = "You are " + (setOp ? "now" : "no longer")
                                    + " operator for NuclearBot. :warning:";

                            StringBuilder sb = new StringBuilder();
                            sb.append(setOp ? "Set" : "Un-set");
                            sb.append(" to operator:");

                            for (IUser mentioned : message.getMentions())
                            {
                                // set op
                                BotUtil.setOperator(mentioned.getID(), setOp);
                                // notify new op
                                BotUtil.sendMessage(mentioned.getOrCreatePMChannel(), notification);
                                sb.append("\n- ");
                                sb.append(mentioned.mention());
                            }

                            BotUtil.sendMessage(pmChannel, sb.toString());
                            // notify executor
                        }
                    }
                }
                catch (DiscordException e)
                {
                    LOGGER.error("Error in bot management command:", e);
                }
            });
        }
    }

}
