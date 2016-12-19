package nuclearcoder.discordbot.command.modules;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.cah.CahConfig;
import nuclearcoder.discordbot.cah.CahGame;
import nuclearcoder.discordbot.cah.card.CahCardDeck;
import nuclearcoder.discordbot.command.Command;
import nuclearcoder.discordbot.database.SqlSingletons;
import nuclearcoder.util.Logger;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CmdCah implements Command {

    private final CahConfig config;
    private final CahGame cah;

    private final AtomicReference<IChannel> channel = new AtomicReference<>();

    public CmdCah(IDiscordClient client)
    {
        this.config = new CahConfig();
        this.cah = new CahGame(config, channel);
        
        /* load up persistent data from database */
        try
        {
            channel.set(client.getChannelByID(SqlSingletons.Cah.getChannel()));
        }
        catch (SQLException e)
        {
            Logger.error("Couldn't set channel from database:");
            Logger.printStackTrace(e);
        }
    }

    @Override public void execute(NuclearBot bot, IMessage message, String originalCommand,
            String[] args)
    {
        String command = args.length > 1 ? args[1] : "help";

        IChannel sentChannel = message.getChannel();

        if (command.equalsIgnoreCase("help"))
        {
            help(args);
        }
        else if (command.equalsIgnoreCase("setchan"))
        {
            setChan(message);
        }
        else if (channel.get() != null && channel.get().getID().equals(sentChannel.getID()))
        {
            boolean hasPerm = BotUtil.hasPermission(message.getAuthor(), message.getGuild(),
                    Permissions.MANAGE_MESSAGES);
            if (hasPerm && command.equalsIgnoreCase("config"))
            {
                config(args);
            }
            else if (hasPerm && command.equalsIgnoreCase("deckadd"))
            {
                deckadd(args);
            }
            else if (hasPerm && command.equalsIgnoreCase("deckremove"))
            {
                deckremove(args);
            }
            else if (hasPerm && command.equalsIgnoreCase("deckrmall"))
            {
                deckrmall();
            }
            else if (hasPerm && command.equalsIgnoreCase("start"))
            {
                start();
            }
            else if (hasPerm && command.equalsIgnoreCase("stop"))
            {
                stop();
            }
            else if (command.equalsIgnoreCase("join"))
            {
                join(message);
            }
            else if (command.equalsIgnoreCase("leave"))
            {
                leave(message);
            }
            else
            {
                pick(message, args);
            }
        }

    }

    private void sendMessage(String message)
    {
        BotUtil.sendMessage(channel.get(), message);
    }

    private void help(String[] args)
    {
        sendMessage("Help is not written yet. Sorryyy~ :slight_frown: ");
        sendMessage(
                "For the time being, here's a list of available commands: ```config, deckadd, deckremove, deckrmall, start, stop, join, leave```");
    }

    private void setChan(IMessage message)
    {
        IChannel sentChannel = message.getChannel();

        channel.set(sentChannel);
        sendMessage("This channel is now hosting Cards against Humanity. :wave: ");

        try
        {
            SqlSingletons.Cah.setChannel(sentChannel.getID());
        }
        catch (SQLException e)
        {
            Logger.error("Couldn't set CaH channel in database:");
            Logger.printStackTrace(e);
        }
    }

    private void config(String[] args)
    {
        if (args.length < 3)
        {
            sendMessage("Available config keys: ```" + "- minPlayers\n" + "- maxScore\n"
                    + "- whiteTimeout\n" + "- blackTimeout\n" + "- timeBetweenRounds\n" + "```");
        }
        else if (args.length == 3)
        {
            String key = args[2];
            if (key.equalsIgnoreCase("save")) // `save` will be a sub-command
            {
                config.save();
                sendMessage("Config was saved. :white_check_mark: ");
            }
            else
            {
                Integer value = config.get(key);
                if (value != null) // filter defined entries
                {
                    sendMessage("Config value: ```" + key + " = " + value + "```");
                }
            }
        }
        else
        {
            if (cah.isRunning())
            {
                sendMessage(
                        "The game is already running, change config when it's over. :warning: ");
            }
            else
            {
                try
                {
                    String key = args[2];
                    if (config.get(key) != null) // filter defined entries
                    {
                        int value = Integer.parseUnsignedInt(args[3]);
                        sendMessage("Config value: ```" + key + " := " + value + "```");
                        config.set(key, value);
                    }
                }
                catch (NumberFormatException e)
                {
                    // fail silently
                }
            }
        }
    }

    private void deckadd(String[] args)
    {
        if (args.length < 3)
        {
            sendMessage("You must provide the Cardcast 5-char code. :warning: ");
        }
        else
        {
            String code = args[2];

            CahCardDeck deck = cah.getCardProvider().addDeck(code);
            if (deck == null)
            {
                sendMessage("Failed loading the `" + code + "` deck. :warning: ");
            }
            else
            {
                sendMessage("Successfully loaded the `" + code + "` deck. :white_check_mark: ");
            }
        }
    }

    private void deckremove(String[] args)
    {
        if (args.length < 3)
        {
            sendMessage("You must provide the Cardcast 5-char code. :warning: ");
        }
        else
        {
            String code = args[2];

            cah.getCardProvider().removeDeck(code);

            sendMessage("Removed the `" + code + "` deck. :white_check_mark: ");
        }
    }

    private void deckrmall()
    {
        cah.getCardProvider().resetDecks();

        sendMessage("Removed all decks. :white_check_mark: ");
    }

    private void start()
    {
        sendMessage("Starting the game... :timer: ");

        cah.start();
    }

    private void stop()
    {
        sendMessage("Forcefully stopping the game... :timer: ");

        cah.stop();
    }

    private void join(IMessage message)
    {
        IUser user = message.getAuthor();
        List<IUser> mentions = message.getMentions();

        if (BotUtil.isOperator(user.getID()) && !mentions.isEmpty())
        {
            for (IUser mention : message.getMentions())
                cah.addPlayer(mention);
        }
        else
        {
            cah.addPlayer(user);
        }
    }

    private void leave(IMessage message)
    {
        IUser user = message.getAuthor();
        List<IUser> mentions = message.getMentions();

        if (BotUtil.isOperator(user.getID()) && !mentions.isEmpty())
        {
            for (IUser mention : mentions)
                cah.removePlayer(mention);
        }
        else
        {
            cah.removePlayer(user);
        }
    }

    private void pick(IMessage message, String[] args)
    {
        if (cah.isRunning())
        {
            try
            {
                int index = Integer.parseInt(args[1]);

                IUser user = message.getAuthor();
                String userID = user.getID();

                int stage = cah.getStage();
                boolean isCzar = cah.isCardCzar(userID);

                if (stage == CahGame.STAGE_WHITE && !isCzar)
                {
                    cah.pickCardWhite(userID, index);
                }
                else if (stage == CahGame.STAGE_CZAR && isCzar)
                {
                    cah.pickCardCzar(userID, index);
                }
            }
            catch (NumberFormatException e)
            {
                // fail silently
            }
        }
    }

}
