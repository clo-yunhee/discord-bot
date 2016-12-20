package nuclearcoder.util;

import nuclearcoder.discordbot.NuclearBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/*
 * Copyright (C) 2016 NuclearCoder
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Static class for a configuration.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/)<br>
 *
 * @author NuclearCoder (contact me on GitHub)
 */
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private static final Properties prop;
    private static final File configFile;

    static
    {
        configFile = new File(NuclearBot.CONFIG_FILENAME);

        if (configFile.isDirectory())
        {
            LOGGER.error("Couldn't write to config.properties in the program's directory.");
            System.exit(1);
        }
        if (!configFile.exists()) // copy the default file if it doesn't exist
        {
            FileOutputStream out = null;
            InputStream in = null;
            try
            {
                in = Config.class.getResourceAsStream("/" + NuclearBot.CONFIG_FILENAME);
                out = new FileOutputStream(configFile);

                byte[] buf = new byte[128];
                while (in.read(buf) != -1)
                {
                    out.write(buf);
                }

                out.close();
                in.close();
            }
            catch (IOException e)
            {
                LOGGER.error("An error occurred while writing default config:", e);
                try
                {
                    if (out != null)
                        out.close();
                }
                catch (IOException silent)
                {
                    // silent
                }
                try
                {
                    if (in != null)
                        in.close();
                }
                catch (IOException silent)
                {
                    // silent
                }
                System.exit(1);
            }
        }

        prop = new Properties();

        FileReader in = null;
        try
        {
            in = new FileReader(configFile);
            prop.load(in);
        }
        catch (IOException e)
        {
            LOGGER.error("An error occurred while loading config.", e);
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException e)
            {
                // silent
            }
        }

    }

    /**
     * Writes the configuration into the file.
     *
     * @throws IOException if the file exists but is a directory rather than a regular
     *                     file, does not exist but cannot be created, or cannot be
     *                     opened for any other reason
     */
    public static void saveConfig() throws IOException
    {
        final FileWriter writer = new FileWriter(configFile);
        prop.store(writer,
                "please do not attempt to edit anything manually unless explicitly directed otherwise");
        try
        {
            writer.close();
        }
        catch (IOException e)
        {
            // silent
        }
    }

    /**
     * Reloads the configuration from the file.
     *
     * @throws IOException if the file does not exist, is a directory rather than a
     *                     regular file, or for some other reason cannot be opened for
     *                     reading.
     */
    public static void reloadConfig() throws IOException
    {
        final FileReader reader = new FileReader(configFile);
        prop.load(reader);
        try
        {
            reader.close();
        }
        catch (IOException e)
        {
            // silent
        }
    }

    /**
     * Returns the property with the specified key in this configuration. If the
     * key is not found in the list, the method returns an empty string and the
     * property is set.
     *
     * @param key the property key
     * @return the value in this property list with the specified key
     * @see #get(String, String)
     */
    public static String get(final String key)
    {
        return get(key, "");
    }

    /**
     * Returns the property with the specified key in this configuration. If the
     * key is not found in the list, the method returns the second argument and
     * the property is set.
     *
     * @param key   the property key
     * @param value a default value
     * @return the value in this property list with the specified key
     */
    public static String get(final String key, final String value)
    {
        if (prop.containsKey(key))
        {
            return prop.getProperty(key);
        }
        else
        {
            prop.setProperty(key, "");
            return "";
        }
    }

    /**
     * Sets the property with the specified key in this configuration with the
     * specified value. This method returns the previous value, or null if there
     * was none.
     *
     * @param key   the property key
     * @param value the new value
     * @return the previous value, or null
     */
    public static String set(final String key, final Object value)
    {
        return (String) prop.setProperty(key, String.valueOf(value));
    }

}
