package nukebot

import sx.blah.discord.modules.IModule
import sx.blah.discord.modules.ModuleLoader
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.jar.JarFile

private fun listModulesDirectory(): Array<File> {
    return MODULES_DIR.listFiles { file, s -> s.endsWith(".jar", true) } ?: emptyArray()
}

private fun loadClass(classLoader: ClassLoader, clazz: String): Class<*> {
    if (clazz.contains("$")) {
        val subString = clazz.substring(0, clazz.lastIndexOf("$"))
        if (subString.isNotEmpty()) {
            try {
                loadClass(classLoader, subString)
            } catch (ignored: ClassNotFoundException) {
                // If the parent class doesn't exist then it is safe to instantiate the child
            }
        }
    }
    return Class.forName(clazz, true, classLoader)
}

private fun loadFile(file: File, loader: ModuleLoader) {
    if (file.isFile && file.name.endsWith(".jar")) { // Can't be a directory and must be a jar
        try {
            JarFile(file).use { jar ->
                val man = jar.manifest
                val moduleAttrib = man.mainAttributes.getValue("Discord4J-ModuleClass") ?: return

                val moduleClasses = moduleAttrib.split(';')

                URLClassLoader(
                        arrayOf(file.toURI().toURL())).use {
                    for (moduleClass in moduleClasses) {
                        val classInstance = loadClass(it, moduleClass)
                        if (IModule::class.java.isAssignableFrom(classInstance)) {
                            val module = classInstance.newInstance() as IModule

                            LOGGER.info("Loading module {} v{} by {}", module.name,
                                    module.version, module.author)

                            loader.loadModule(module)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalAccessException,
                is InstantiationException,
                is ClassNotFoundException,
                is IOException
                -> LOGGER.error("Unable to load module " + file.name, e)
                else -> throw e
            }
        }

    }
}

internal fun reloadModules(loader: ModuleLoader) {
    // unload them
    loader.loadedModules.forEach { loader.unloadModule(it) }

    // load new
    val files = listModulesDirectory()

    LOGGER.info("Attempting to (re-)load {} module(s)...", files.size)
    if (files.isNotEmpty()) {
        files.forEach { loadFile(it, loader) }
    }
}
