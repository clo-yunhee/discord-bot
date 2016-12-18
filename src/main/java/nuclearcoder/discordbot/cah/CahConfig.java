package nuclearcoder.discordbot.cah;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nuclearcoder.discordbot.database.SqlSingletons;
import nuclearcoder.util.Logger;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CahConfig {

    public static final String MINIMUM_PLAYERS = "minPlayers";
    public static final String MAXIMUM_SCORE = "maxScore";
    public static final String WHITE_TIMEOUT = "whiteTimeout";
    public static final String BLACK_TIMEOUT = "blackTimeout";
    public static final String TIME_BETWEEN_ROUNDS = "timeBetweenRounds";

    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {
    }.getType();

    private Map<String, Integer> config;

    public CahConfig()
    {
        this.config = new HashMap<String, Integer>();
        config.put(MINIMUM_PLAYERS, 4);
        config.put(MAXIMUM_SCORE, 8);
        config.put(WHITE_TIMEOUT, (int) TimeUnit.MINUTES.toSeconds(2));
        config.put(BLACK_TIMEOUT, (int) TimeUnit.MINUTES.toSeconds(1));
        config.put(TIME_BETWEEN_ROUNDS, (int) TimeUnit.SECONDS.toSeconds(10));

        load();
    }

    private static final String beautify(int time)
    {
        StringBuilder sb = new StringBuilder();

        int timeMin = time / 60;
        int timeSec = time % 60;

        if (timeMin > 0)
        {
            sb.append(timeMin);
            sb.append(" minutes");
            if (timeSec > 0)
                sb.append(" and ");
        }
        if (timeMin == 0 || timeSec > 0)
        {
            sb.append(timeSec);
            sb.append(" seconds");
        }

        return sb.toString();
    }

    public void save()
    {
        try
        {
            Gson gson = new Gson();
            String data = gson.toJson(config, MAP_TYPE);

            SqlSingletons.Cah.setConfig(data);
        }
        catch (SQLException e)
        {
            Logger.error("Couldn't save CaH config:");
            Logger.printStackTrace(e);
        }
    }

    public void load()
    {
        try
        {
            String data = SqlSingletons.Cah.getConfig();
            Gson gson = new Gson();

            Map<String, Integer> deserialized = gson.fromJson(data, MAP_TYPE);
            if (deserialized != null)
                config.putAll(deserialized);
        }
        catch (SQLException e)
        {
            Logger.error("Couldn't load CaH config:");
            Logger.printStackTrace(e);
        }
    }

    public Integer get(String key)
    {
        return config.get(key);
    }

    public String getString(String key)
    {
        return beautify(get(key));
    }

    public void set(String key, int value)
    {
        config.put(key, value);
    }

}
