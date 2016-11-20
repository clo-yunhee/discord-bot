package nuclearcoder.discordbot.commands;

import nuclearcoder.discordbot.Bot;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;

public class CmdManageBot implements Command {
	
	@Override
	public void execute(Bot bot, IMessage message, String command, String[] args) throws Exception
	{
		IUser user = message.getAuthor();
		
		if (bot.isOperator(user))
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
					String notification = "You " + (setOp ? "now" : "no longer") + " are operator for NuclearBot. :warning:";
					
					StringBuilder sb = new StringBuilder();
					sb.append(setOp ? "Set" : "Un-set");
					sb.append(" to operator:");
					
					for (IUser mentioned : message.getMentions())
					{
						bot.setOperator(mentioned, setOp); // set op
						mentioned.getOrCreatePMChannel().sendMessage(notification); // notify new op
						sb.append("\n- " + mentioned.mention());
					}
					
					pmChannel.sendMessage(sb.toString()); // notify executor
				}
			}
		}
	}

}
