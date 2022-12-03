package com.guflimc.brick.libloader.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class SpigotBrickLibLoader extends JavaPlugin {

    public SpigotBrickLibLoader() {
        super();

        getLogger().info("Checking plugins for custom repositories.");

        File pluginDir = getDataFolder().getParentFile();
        for (File file : pluginDir.listFiles()) {
            if ( !file.isFile() || !file.getName().endsWith(".jar")) {
                continue;
            }

            try {
                loadLibraries(file);
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Unable to load external dependencies for " + file.getName() + ".", ex);
            }
        }
    }

    private void loadLibraries(File file) {
        YamlConfiguration pluginyml;
        try (JarFile jar = new JarFile(file);
             InputStream is = jar.getInputStream(jar.getJarEntry("plugin.yml"));
             InputStreamReader isr = new InputStreamReader(is)) {
            pluginyml = YamlConfiguration.loadConfiguration(isr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String name = pluginyml.getString("name");
        ConfigurationSection repos = pluginyml.getConfigurationSection("repositories");

        if ( name == null || repos == null ) {
            return;
        }

        getLogger().info("Checking libraries for " + name + ".");
        PluginLibraryLoader loader = new PluginLibraryLoader(getLogger());

        for (String key : repos.getKeys(false)) {
            loader.addRepository(key, pluginyml.getString("repositories." + key));
        }

        if ( pluginyml.contains("libraries") ) {
            loader.load(pluginyml.getStringList("libraries"));
        }
    }

}
