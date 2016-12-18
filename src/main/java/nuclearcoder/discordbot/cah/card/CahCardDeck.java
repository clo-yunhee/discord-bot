package nuclearcoder.discordbot.cah.card;

import java.util.List;

public class CahCardDeck {

    private String code;
    private String name;
    private String description;

    private List<CahBlackCard> blackCards;
    private List<CahWhiteCard> whiteCards;

    public CahCardDeck(String code, String name, String description, List<CahBlackCard> blackCards,
            List<CahWhiteCard> whiteCards)
    {
        this.code = code;
        this.name = name;
        this.description = description;
        this.blackCards = blackCards;
        this.whiteCards = whiteCards;
    }

    public String getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<CahBlackCard> getBlackCards()
    {
        return blackCards;
    }

    public List<CahWhiteCard> getWhiteCards()
    {
        return whiteCards;
    }

}
