package nuclearcoder.discordbot.cah;

import nuclearcoder.discordbot.cah.card.CahWhiteCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.List;

public class CahPlayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CahPlayer.class);

    private final IUser user;
    private final String userID;

    private final List<CahWhiteCard> hand;
    private IMessage handMessage;

    private int pickedCardIndex;

    private int points;

    CahPlayer(IUser user)
    {
        this.user = user;
        this.userID = user.getID();

        this.points = 0;
        this.hand = new ArrayList<>(10);

        RequestBuffer.request(() ->
        {
            try
            {
                this.handMessage = user.getOrCreatePMChannel().sendMessage("Your hand:\n");
            }
            catch (Exception e)
            {
                LOGGER.error("Couldn't create hand message:", e);
            }
        });
    }

    public IUser getUser()
    {
        return user;
    }

    String getUserID()
    {
        return userID;
    }

    int retrievePickedCard()
    {
        int card = pickedCardIndex;
        pickedCardIndex = 0;
        return card;
    }

    int getPoints()
    {
        return points;
    }

    int getCardCount()
    {
        return hand.size();
    }

    boolean setPickedCard(int index, int size)
    {
        this.pickedCardIndex = index;
        return index > 0 && index <= size;
    }

    void addCard(CahWhiteCard card, boolean doUpdate)
    {
        hand.add(card);
        if (doUpdate)
            updateHandMessage();
    }

    CahWhiteCard removeCard(int index, boolean doUpdate)
    {
        CahWhiteCard card = hand.remove(index);
        if (doUpdate)
            updateHandMessage();
        return card;
    }

    void addPoint()
    {
        points++;
    }

    void reset()
    {
        if (handMessage != null)
        {
            RequestBuffer.request(() ->
            {
                try
                {
                    handMessage.delete();
                }
                catch (Exception e)
                {
                    LOGGER.error("Couldn't delete hand message:", e);
                }
            });
        }
    }

    void updateHandMessage()
    {
        if (handMessage == null)
        {
            RequestBuffer.request(() ->
            {
                try
                {
                    handMessage = user.getOrCreatePMChannel().sendMessage("Your hand:\n");
                }
                catch (Exception e)
                {
                    LOGGER.error("Couldn't create hand message:", e);
                }
            });
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Your hand:\n");
        sb.append("```");
        int i = 1;
        for (CahWhiteCard card : hand)
        {
            sb.append(i++);
            sb.append(": ");
            sb.append(card.getText());
            sb.append('\n');
        }
        sb.append("```");

        final String text = sb.toString();

        RequestBuffer.request(() ->
        {
            try
            {
                handMessage.edit(text);
            }
            catch (Exception e)
            {
                LOGGER.error("Couldn't update hand message:", e);
            }
        });
    }

}
