package nuclearcoder.discordbot.cah;

import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.cah.card.CahBlackCard;
import nuclearcoder.discordbot.cah.card.CahCardProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CahGame {

    public static final int STAGE_START = 0;
    public static final int STAGE_WHITE = 1;
    public static final int STAGE_CZAR = 2;
    public static final int STAGE_POINTS = 3;
    public static final int STAGE_FINISH = 4;
    private static final Logger LOGGER = LoggerFactory.getLogger(CahGame.class);
    private final AtomicReference<IChannel> channel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CahCardProvider cards;
    private final CahConfig config;

    private Timer timer;
    
    /* ongoing game variables */

    private LinkedList<String> playerDeque;
    private Map<String, CahPlayer> players;
    
    /* ongoing round variables */

    private int stage; // one of the STAGE_*** constants
    private String czar; // the id of the czar

    private TaskWaitWhite taskWaitWhite;
    private String[] pickPlayers; // card_index -> player_id
    private String[] pickCards; // card_index -> card_text
    private int pickRemaining;

    private TaskWaitCzar taskWaitCzar;

    private String roundWinner;
    private String roundWinnerCard;

    public CahGame(CahConfig config, AtomicReference<IChannel> channel)
    {
        this.channel = channel;
        this.cards = new CahCardProvider();
        this.config = config;
        this.timer = new Timer();

        this.playerDeque = new LinkedList<>();
        this.players = new HashMap<>();
    }

    public void addPlayer(IUser user)
    {
        String userID = user.getID();
        if (!players.containsKey(userID))
        {
            playerDeque.add(userID);
            players.put(userID, new CahPlayer(user));

            sendMessage(user.mention() + " joined CaH. :wave: ");
        }
        else
        {
            sendMessage(user.mention() + " already joined CaH. :warning: ");
        }
    }

    public void removePlayer(IUser user)
    {
        String userID = user.getID();
        if (players.containsKey(userID))
        {
            playerDeque.remove(userID);
            players.remove(userID);

            sendMessage(user.mention() + " left CaH. :wave: ");
        }
        else
        {
            sendMessage(user.mention() + " didn't join CaH. :warning: ");
        }
    }

    public boolean isCardCzar(String userID)
    {
        return czar.equals(userID);
    }

    public void pickCardWhite(String userID, int index)
    {
        try
        {
            CahPlayer player = players.get(userID);
            if (player.setPickedCard(index, player.getCardCount()))
            {
                if (--pickRemaining == 0)
                {
                    taskWaitWhite.cancel();
                    taskWaitWhite.run();
                }
            }
            else
            {
                sendMessage("You must enter a number that matches a card. :warning: ");
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Couldn't update hand message:", e);
        }
    }

    public void pickCardCzar(String userID, int index)
    {
        if (players.get(userID).setPickedCard(index, pickPlayers.length))
        {
            taskWaitCzar.cancel();
            taskWaitCzar.run();
        }
        else
        {
            sendMessage("You must enter a number that matches a card. :warning: ");
        }
    }

    public void start()
    {
        if (running.get())
        {
            sendMessage("The game is already running :warning: ");
            return;
        }

        if (playerDeque.size() < config.get(CahConfig.MINIMUM_PLAYERS))
        {
            sendMessage("Not enough players, the game needs at least " + config
                    .get(CahConfig.MINIMUM_PLAYERS) + " players to start. :warning: ");
            return;
        }
        
        /* shuffle player list before start */
        Collections.shuffle(playerDeque);
        
        /* draw ten initial cards */
        for (CahPlayer player : players.values())
        {
            for (int i = 0; i < 10; i++)
                player.addCard(cards.pickWhiteCard(), false);
            player.updateHandMessage();
        }

        running.set(true);
        round0Start();
    }

    public void stop()
    {
        if (running.get())
        {
            running.set(false);

            timer.cancel();
            playerDeque.clear();

            Iterator<CahPlayer> it = this.players.values().iterator();
            while (it.hasNext())
            {
                it.next().reset();
                it.remove();
            }

            sendMessage("The game is over or has been stopped. :octagonal_sign: ");
        }
    }

    public boolean isRunning()
    {
        return running.get();
    }

    public int getStage()
    {
        return stage;
    }

    public CahCardProvider getCardProvider()
    {
        return cards;
    }

    private void sendMessage(final String message)
    {
        BotUtil.sendMessage(channel.get(), message);
    }

    private String mention(String userID)
    {
        return players.get(userID).getUser().mention();
    }

    /*
     * A round is the following: - next player is Card Czar - pick a black card
     * - wait WHITE_TIMEOUT or till every player picked a white card - wait
     * BLACK_TIMEOUT or till the Card Czar picked a white card - dispense points
     * - if the player has more than MAX_POINTS then end the game - else rotate
     * the player list and start another round
     */
    private void round0Start()
    {
        stage = STAGE_START;
        
        /* new card czar */

        this.czar = playerDeque.removeFirst();
        playerDeque.addLast(czar);

        sendMessage("Card Czar is " + mention(czar));
        
        /* pick black card */

        CahBlackCard card = cards.pickBlackCard();

        sendMessage(
                ":black_large_square: the black card for this round is: \n" + "```" + card.getText()
                        + "```");

        // TODO: add multi-pick support

        round1WhitePick();
    }

    private void round1WhitePick()
    {
        stage = STAGE_WHITE;
        
        /* wait till everyone picked or timeout */

        sendMessage(":white_large_square: you have " + config.getString(CahConfig.WHITE_TIMEOUT)
                + " to pick a white card.");

        pickRemaining = players.size() - 1;
        pickPlayers = new String[pickRemaining];
        pickCards = new String[pickRemaining];

        timer.schedule(taskWaitWhite = new TaskWaitWhite(),
                config.get(CahConfig.WHITE_TIMEOUT) * 1000L);
    }

    private void round2CzarPick()
    {
        stage = STAGE_CZAR;
        
        /* wait till Czar picked or timeout */

        for (CahPlayer player : players.values())
        {
            player.addCard(cards.pickWhiteCard(), true);
        }

        // shuffle the set
        List<CahPlayer> shuffledPlayerList = new ArrayList<>(players.values());
        Collections.shuffle(shuffledPlayerList);

        StringBuilder sb = new StringBuilder();
        sb.append("Picked cards:\n");
        sb.append("```");
        int i = 0;
        for (CahPlayer player : shuffledPlayerList)
        {
            String userID = player.getUserID();
            if (!czar.equals(userID))
            {
                int card = player.retrievePickedCard() - 1;
                if (card >= 0)
                {
                    pickPlayers[i] = userID;
                    pickCards[i] = player.removeCard(card, true).getText();

                    sb.append(i + 1);
                    sb.append(": ");
                    sb.append(pickCards[i]);
                    sb.append('\n');

                    i++;
                }
            }
        }
        sb.append("```");

        if (i == 0) // this means no player has picked
        {
            sendMessage("No card was picked by any player. Skipping this round. :arrow_forward: ");

            round4Finish();
        }
        else
        {
            sendMessage(sb.toString());

            sendMessage("Card Czar " + mention(czar) + " has " + config
                    .getString(CahConfig.BLACK_TIMEOUT) + " to choose the best card.");

            timer.schedule(taskWaitCzar = new TaskWaitCzar(),
                    config.get(CahConfig.BLACK_TIMEOUT) * 1000L);
        }
    }

    private void round3Points()
    {
        stage = STAGE_POINTS;
        
        /* dispense points */

        int winnerCard = players.get(czar).retrievePickedCard() - 1;
        if (winnerCard < 0)
        {
            roundWinner = null;

            sendMessage("Card Czar " + mention(czar)
                    + " did not pick, skipping this round. :arrow_forward: ");
        }
        else
        {
            roundWinner = pickPlayers[winnerCard];
            roundWinnerCard = pickCards[winnerCard];

            sendMessage("The chosen card is ```" + roundWinnerCard + "```");

            players.get(roundWinner).addPoint();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Points: \n```");
        for (CahPlayer player : players.values())
        {
            sb.append(player.getUser().getDisplayName(channel.get().getGuild()));
            sb.append(" - ");
            sb.append(player.getPoints());
            sb.append(" point(s) \n");
        }
        sb.append("```");
        sendMessage(sb.toString());

        round4Finish();
    }

    private void round4Finish()
    {
        stage = STAGE_FINISH;
        
        /* if there's a winner, end the game */

        if (roundWinner != null && players.get(roundWinner).getPoints() >= config
                .get(CahConfig.MAXIMUM_SCORE))
        {
            sendMessage("This game's winner is " + mention(roundWinner) + " :trophy: ");
            stop();
        }
        
        /* else, start another round */

        else
        {
            sendMessage("Waiting " + config.getString(CahConfig.TIME_BETWEEN_ROUNDS)
                    + " before next round...");
            timer.schedule(new TaskStartRound(), config.get(CahConfig.TIME_BETWEEN_ROUNDS) * 1000L);
        }
    }
    
    /* timer tasks */

    private class TaskWaitWhite extends TimerTask {
        @Override public void run()
        {
            round2CzarPick();
        }
    }

    private class TaskWaitCzar extends TimerTask {
        @Override public void run()
        {
            round3Points();
        }
    }

    private class TaskStartRound extends TimerTask {
        @Override public void run()
        {
            round0Start();
        }
    }

}
