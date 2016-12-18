package nuclearcoder.discordbot.cah;

import nuclearcoder.discordbot.cah.card.CahWhiteCard;
import nuclearcoder.util.Logger;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.List;

public class CahPlayer {

    private final IUser user;
    private final String userID;

    private final List<CahWhiteCard> hand;
    private IMessage handMessage;

    private int pickedCardIndex;

    private int points;

    public CahPlayer(IUser user)
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
                Logger.error("Couldn't create hand message:");
                Logger.printStackTrace(e);
            }
        });
    }

    public IUser getUser()
    {
        return user;
    }

    public String getUserID()
    {
        return userID;
    }

    public int retrievePickedCard()
    {
        int card = pickedCardIndex;
        pickedCardIndex = 0;
        return card;
    }

    public int getPoints()
    {
        return points;
    }

    public int getCardCount()
    {
        return hand.size();
    }

    public boolean setPickedCard(int index, int size)
    {
        this.pickedCardIndex = index;
        return index > 0 && index <= size;
    }

    public void addCard(CahWhiteCard card, boolean doUpdate)
    {
        hand.add(card);
        if (doUpdate)
            updateHandMessage();
    }

    public CahWhiteCard removeCard(int index, boolean doUpdate)
    {
        CahWhiteCard card = hand.remove(index);
        if (doUpdate)
            updateHandMessage();
        return card;
    }

    public void addPoint()
    {
        points++;
    }

    public void reset()
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
                    Logger.error("Couldn't delete hand message:");
                    Logger.printStackTrace(e);
                }
            });
        }
    }

    public void updateHandMessage()
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
                    Logger.error("Couldn't create hand message:");
                    Logger.printStackTrace(e);
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
                Logger.error("Couldn't update hand message:");
                Logger.printStackTrace(e);
            }
        });
    }

}
