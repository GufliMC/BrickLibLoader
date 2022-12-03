package com.guflimc.brick.libloader.spigot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarFile;

public class SpigotBrickLibLoader extends JavaPlugin {

    private final SpigotBrickLibraryLoader loader;

    public SpigotBrickLibLoader() {
        super();

        loader = new SpigotBrickLibraryLoader(getLogger());

        File pluginDir = getDataFolder().getParentFile();
        for (File file : pluginDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                loadLibraries(file);
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

        if (pluginyml.contains("repositories")) {
            for (String key : pluginyml.getConfigurationSection("repositories").getKeys(false)) {
                loader.addRepository(key, pluginyml.getString("repositories." + key));
            }
        }

        if (pluginyml.contains("libraries")) {
            loader.load(pluginyml.getString("name"), pluginyml.getStringList("libraries"));
        }
    }

}
