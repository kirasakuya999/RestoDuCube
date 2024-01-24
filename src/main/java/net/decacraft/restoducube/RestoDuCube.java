package net.decacraft.restoducube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class RestoDuCube extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    public static RestoDuCube instance;
    public static HashMap<UUID, String[]> commands = new HashMap<>();

    public static TextComponent prefix = Component.text("[RESTO] ").color(NamedTextColor.YELLOW);
    public static String chestName = "Recyclage";

    @Override
    public void onEnable() {
        log.info("Ouverture des Restos du Cube");

        super.onEnable();
        instance = this;

        //Initialisation de la config des items
        createItemConfig();

        //Déclaration des listener pour les commandes saisies
        CommandListener listener = new CommandListener(this);
        getCommand("resto").setExecutor(listener);
        getCommand("resto").setTabCompleter(listener);

        //Initialisation de l'observateur d'events
        getServer().getPluginManager().registerEvents(new EventManager(), this);
    }

    @Override
    public void onDisable() {
        log.info("Fermeture des Restos du Cube");
        this.saveConfig();
    }

    //Fichier de config customisé, avec les items par catégories
    private File itemsConfigFile;
    private FileConfiguration itemsConfig;
    public FileConfiguration getItemConfig() {
        return this.itemsConfig;
    }
    private void createItemConfig() {
        itemsConfigFile = new File(getDataFolder(), "items.yml");
        if (!itemsConfigFile.exists()) {
            itemsConfigFile.getParentFile().mkdirs();
            saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsConfigFile);
    }
}
