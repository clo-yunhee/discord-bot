package nuclearcoder.discordbot.cah.card;

public class CahWhiteCard {

    private String text;
    private boolean isBlank;

    public CahWhiteCard(String text, boolean isBlank)
    {
        this.text = text;
        this.isBlank = isBlank;
    }

    public String getText()
    {
        return text;
    }

    public boolean isBlank()
    {
        return isBlank;
    }

}
