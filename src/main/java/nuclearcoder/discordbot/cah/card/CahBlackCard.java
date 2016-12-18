package nuclearcoder.discordbot.cah.card;

public class CahBlackCard {

    private String text;
    private int draw;
    private int pick;

    public CahBlackCard(String text, int draw, int pick)
    {
        this.text = text;
        this.draw = draw;
        this.pick = pick;
    }

    public String getText()
    {
        return text;
    }

    public int getDraw()
    {
        return draw;
    }

    public int getPick()
    {
        return pick;
    }

}
