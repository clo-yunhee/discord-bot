package nukeutils

import nukebot.CONFIG_FILENAME
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*

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
 * Static class for a configuration.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/)<br></br>

 * @author NuclearCoder (contact me on GitHub)
 */
object Config {

    private val LOGGER = LoggerFactory.getLogger(Config::class.java)

    private val prop = Properties()
    private val configFile = File(CONFIG_FILENAME)

    init {
        if (configFile.isDirectory) {
            LOGGER.error("Couldn't write to config.properties in the program's directory")
            System.exit(1)
        }
        if (!configFile.exists())
        // copy the default file if it doesn't exist
        {
            try {
                Config::class.java.getResourceAsStream("/" + CONFIG_FILENAME).use { input ->
                    {
                        FileOutputStream(configFile).use { output ->
                            val buf = ByteArray(128)
                            while (input?.read(buf) != -1) {
                                output.write(buf)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                LOGGER.error("Could not write default config", e)
                System.exit(1)
            }
        }

        try {
            FileReader(configFile).use {
                prop.load(it)
            }
        } catch (e: IOException) {
            LOGGER.error("Could not load config", e)
        }
    }

    /**
     * Writes the configuration into the file.

     * @throws IOException if the file exists but is a directory rather than a regular
     * *                     file, does not exist but cannot be created, or cannot be
     * *                     opened for any other reason
     */
    @Throws(IOException::class)
    fun saveConfig() {
        FileWriter(configFile).use {
            prop.store(it, "do not edit manually")
        }
    }

    /**
     * Reloads the configuration from the file.

     * @throws IOException if the file does not exist, is a directory rather than a
     * *                     regular file, or for some other reason cannot be opened for
     * *                     reading.
     */
    @Throws(IOException::class)
    fun reloadConfig() {
        FileReader(configFile).use { prop.load(it) }
    }

    /**
     * Returns the property with the specified key in this configuration. If the
     * key is not found in the list, the method returns the second argument and
     * the property is set.

     * @param key   the property key
     * *
     * @param value a default value
     * *
     * @return the value in this property list with the specified key
     */
    @JvmOverloads operator fun get(key: String, value: String = ""): String {
        return if (prop.containsKey(key)) {
            prop.getProperty(key)
        } else {
            prop.setProperty(key, value)
            value
        }
    }

    /**
     * Sets the property with the specified key in this configuration with the
     * specified value. This method returns the previous value, or null if there
     * was none.

     * @param key   the property key
     * *
     * @param value the new value
     * *
     * @return the previous value, or null
     */
    operator fun set(key: String, value: Any): String {
        return prop.setProperty(key, value.toString()) as String
    }

}