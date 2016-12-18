package nuclearcoder.discordbot.cah.card;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.database.SqlSingletons;
import nuclearcoder.util.Logger;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class CahCardProvider {

    public static final String HOSTNAME = "api.cardcastgame.com";
    private static final String BASE_URL = "https://" + HOSTNAME + "/v1/decks/";
    private static final String INFO_URL_FORMAT = BASE_URL + "%s";
    private static final String CARDS_URL_FORMAT = INFO_URL_FORMAT + "/cards";
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("[A-Z0-9]{5}");
    // store in both Map and List for faster access
    private Map<String, CahCardDeck> mapLoadedDecks;
    private List<CahCardDeck> loadedDecks;
    private Random random;

    public CahCardProvider()
    {
        this.mapLoadedDecks = new HashMap<>();
        this.loadedDecks = new ArrayList<>(4);

        this.random = new Random();

        try
        {
            for (String code : SqlSingletons.Cah.getDecks())
            {
                addDeck(code);
            }
        }
        catch (SQLException e)
        {
            Logger.error("Couldn't load decks from database:");
            Logger.printStackTrace(e);
        }
    }

    public CahCardDeck addDeck(String code)
    {
        if (mapLoadedDecks.containsKey(code))
            return null;
        CahCardDeck deck = loadSet(code);
        if (deck != null)
        {
            mapLoadedDecks.put(code, deck);
            loadedDecks.add(deck);

            try
            {
                SqlSingletons.Cah.setDecks(loadedDecks);
            }
            catch (SQLException e)
            {
                Logger.error("Couldn't save decks in database:");
                Logger.printStackTrace(e);
            }
        }
        return deck;
    }

    public void removeDeck(String code)
    {
        if (mapLoadedDecks.containsKey(code))
        {
            CahCardDeck deck = mapLoadedDecks.remove(code);
            loadedDecks.remove(deck);

            try
            {
                SqlSingletons.Cah.setDecks(loadedDecks);
            }
            catch (SQLException e)
            {
                Logger.error("Couldn't save decks in database:");
                Logger.printStackTrace(e);
            }
        }
    }

    public void resetDecks()
    {
        mapLoadedDecks.clear();
        loadedDecks.clear();
    }

    public void reseedRandomGenerator()
    {
        random = new Random();
    }

    public CahBlackCard pickBlackCard()
    {
        // the two pick-card functions are quite costly in time
        return randomItemFrom(randomItemFrom(loadedDecks).getBlackCards());
    }

    public CahWhiteCard pickWhiteCard()
    {
        return randomItemFrom(randomItemFrom(loadedDecks).getWhiteCards());
    }

    private <T> T randomItemFrom(List<T> list)
    {
        return list.get(random.nextInt(list.size()));
    }

    private CahCardDeck loadSet(String code)
    {
        if (!VALID_ID_PATTERN.matcher(code).matches())
        {
            return null;
        }

        try
        {
            Gson builder = new Gson();
            Type typeDeckInfo = new TypeToken<CardcastDeckInfo>() {
            }.getType();
            Type typeDeck = new TypeToken<CardcastDeck>() {
            }.getType();

            String infoContent = getUrlContent(String.format(INFO_URL_FORMAT, code));
            if (infoContent == null)
            {
                // failed to load
                return null;
            }

            CardcastDeckInfo info = builder.fromJson(infoContent, typeDeckInfo);
            if (info.name == null || info.description == null || info.description.isEmpty())
            {
                // We require a name. Blank description is acceptable, but
                // cannot be null.
                return null;
            }

            String cardContent = getUrlContent(String.format(CARDS_URL_FORMAT, code));
            if (cardContent == null)
            {
                // failed to load
                return null;
            }

            CardcastDeck cards = builder.fromJson(cardContent, typeDeck);

            // load up the black cards
            List<CahBlackCard> blackCards = new ArrayList<>(cards.calls.size());
            if (cards.calls != null)
            {
                for (CardcastCard black : cards.calls)
                {
                    List<String> texts = black.text;
                    if (texts != null)
                    {
                        // TODO: ugly piece of code
                        List<String> strs = new ArrayList<String>(texts.size());
                        for (String bitOfText : texts)
                        {
                            strs.add((String) bitOfText);
                        }
                        String text = StringUtils.join(strs, "_____");
                        int pick = strs.size() - 1;
                        int draw = (pick >= 3 ? pick - 1 : 0);

                        blackCards.add(new CahBlackCard(text, draw, pick));
                    }
                }
            }

            // load up the white cards
            List<CahWhiteCard> whiteCards = new ArrayList<>(cards.responses.size());
            if (cards.responses != null)
            {
                for (CardcastCard white : cards.responses)
                {
                    List<String> texts = white.text;
                    if (texts != null)
                    {
                        // The white cards should only ever have one element in
                        // text, but let's be safe.
                        final List<String> strs = new ArrayList<String>(texts.size());
                        for (final String bitOfText : texts)
                        {
                            final String cardCastString = (String) bitOfText;
                            if (cardCastString.isEmpty())
                            {
                                // skip blank segments
                                continue;
                            }
                            final StringBuilder prettyString = new StringBuilder();

                            // Cardcast's recommended format is to not
                            // capitalize the first letter
                            prettyString.append(cardCastString.substring(0, 1).toUpperCase());
                            prettyString.append(cardCastString.substring(1));

                            // Cardcast's recommended format is to not include a
                            // period
                            if (Character.isLetterOrDigit(
                                    cardCastString.charAt(cardCastString.length() - 1)))
                            {
                                prettyString.append('.');
                            }

                            // Cardcast's white cards are now all pretty
                            strs.add(prettyString.toString());
                        }
                        String text = StringUtils.join(strs, "");

                        whiteCards.add(new CahWhiteCard(text, text.isEmpty()));
                    }
                }
            }

            return new CahCardDeck(code, info.name, info.description, blackCards, whiteCards);
        }
        catch (final Exception e)
        {
            Logger.error(String.format("Unable to load deck %s from Cardcast:", code));
            Logger.printStackTrace(e);
            return null;
        }
    }

    private String getUrlContent(final String urlStr) throws IOException
    {
        final URL url = new URL(urlStr);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(true);
        conn.setReadTimeout(NuclearBot.GET_TIMEOUT);
        conn.setConnectTimeout(NuclearBot.GET_TIMEOUT);

        final int code = conn.getResponseCode();
        if (HttpURLConnection.HTTP_OK != code)
        {
            Logger.error(
                    String.format("Got HTTP response code %d from Cardcast for %s", code, urlStr));
            return null;
        }
        final String contentType = conn.getContentType();
        if (!"application/json".equals(contentType))
        {
            Logger.error(
                    String.format("Got content-type %s from Cardcast for %s", contentType, urlStr));
            return null;
        }

        final InputStream is = conn.getInputStream();
        final InputStreamReader isr = new InputStreamReader(is);
        final BufferedReader reader = new BufferedReader(isr);
        final StringBuilder builder = new StringBuilder(4096);
        String line;
        while ((line = reader.readLine()) != null)
        {
            builder.append(line);
            builder.append('\n');
        }
        reader.close();
        isr.close();
        is.close();

        return builder.toString();
    }

    private class CardcastDeckInfo {
        public String name;
        public String description;
    }

    private class CardcastDeck {
        public List<CardcastCard> calls;
        public List<CardcastCard> responses;
    }

    private class CardcastCard {
        private List<String> text;
    }

}
