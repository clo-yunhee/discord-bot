package nuclearcoder.discordbot;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.modules.ModuleLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class CustomModuleLoader {

    private static final File MODULES_DIR = new File("modules");
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomModuleLoader.class);

    private static final File[] listModulesDirectory()
    {
        return MODULES_DIR.listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter("jar"));
    }

    private static Class loadClass(ClassLoader classLoader, String clazz)
            throws ClassNotFoundException
    {
        if (clazz.contains("$") && clazz.substring(0, clazz.lastIndexOf("$")).length() > 0)
        {
            try
            {
                loadClass(classLoader, clazz.substring(0, clazz.lastIndexOf("$")));
            }
            catch (ClassNotFoundException ignored)
            {
                // If the parent class doesn't exist then it is safe to instantiate the child
            }
        }
        return Class.forName(clazz, true, classLoader);
    }

    private static final void loadFile(File file, ModuleLoader loader)
    {
        if (file.isFile() && file.getName().endsWith(".jar"))
        { // Can't be a directory and must be a jar
            try (JarFile jar = new JarFile(file))
            {
                Manifest man = jar.getManifest();
                String moduleAttrib = man.getMainAttributes().getValue("Discord4J-ModuleClass");

                if (moduleAttrib != null)
                {
                    String[] moduleClasses = moduleAttrib.split(";");

                    try (URLClassLoader classLoader = new URLClassLoader(
                            new URL[] { file.toURI().toURL() }))
                    {
                        for (String moduleClass : moduleClasses)
                        {
                            Class classInstance = loadClass(classLoader, moduleClass);
                            if (IModule.class.isAssignableFrom(classInstance))
                            {
                                IModule module = (IModule) classInstance.newInstance();

                                LOGGER.info("Loading module {} v{} by {}", module.getName(),
                                        module.getVersion(), module.getAuthor());

                                loader.loadModule(module);
                            }
                        }
                    }
                }
            }
            catch (IllegalAccessException | InstantiationException | ClassNotFoundException | IOException e)
            {
                LOGGER.error("Unable to load module " + file.getName() + "!", e);
            }
        }
    }

    static final void reloadModules(ModuleLoader loader)
    {
        // unload them
        for (IModule module : loader.getLoadedModules())
            loader.unloadModule(module);

        // load new
        File[] files = listModulesDirectory();
        if (files != null && files.length > 0)
        {
            LOGGER.info("Attempting to (re-)load {} module(s)...", files.length);

            for (File file : files)
                loadFile(file, loader);
        }
    }

}
