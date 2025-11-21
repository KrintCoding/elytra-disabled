package ua.krint.elytraDisabled;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ElytraDisabled extends JavaPlugin {

    private FileConfiguration config;
    private FileConfiguration langConfig;
    private ElytraListener listener;
    private ElytraTickChecker tickChecker;
    private String currentLanguage;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        config = getConfig();

        setupLanguage();

        listener = new ElytraListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);

        startTickChecker();

        ElytraCommand command = new ElytraCommand(this);
        getCommand("elytra-disabled").setExecutor(command);
        getCommand("elytra-disabled").setTabCompleter(command);

        startCooldownCleanup();

        getLogger().info("ElytraDisabled успешно загружен!");
        getLogger().info("Язык: " + currentLanguage);
        getLogger().info("Защита активна в мирах: " + config.getStringList("settings.disable_in_worlds"));
    }

    private void startCooldownCleanup() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (listener != null) {
                listener.cleanUpCooldowns();
            }
        }, 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        if (tickChecker != null) {
            tickChecker.stop();
        }
        getLogger().info("ElytraDisabled отключён!");
    }

    private void setupLanguage() {
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveResourceIfNotExists("lang/ru.yml");
        saveResourceIfNotExists("lang/en.yml");
        saveResourceIfNotExists("lang/ua.yml");

        currentLanguage = config.getString("settings.language", "ru");
        File langFile = new File(langFolder, currentLanguage + ".yml");

        if (!langFile.exists()) {
            getLogger().warning("Файл языка '" + currentLanguage + ".yml' не найден! Используется русский язык.");
            currentLanguage = "ru";
            langFile = new File(langFolder, "ru.yml");
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        try {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(getResource("lang/" + currentLanguage + ".yml"), StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultConfig);
            langConfig.options().copyDefaults(true);

            langConfig.save(langFile);
        } catch (Exception e) {
            getLogger().warning("Не удалось загрузить дефолтные значения для языка: " + currentLanguage);
            e.printStackTrace();
        }
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    public boolean isPluginEnabled() {
        return config.getBoolean("settings.enable_plugin", true);
    }

    public boolean isWorldDisabled(World world) {
        if (!isPluginEnabled()) return false;

        List<String> disabledWorlds = config.getStringList("settings.disable_in_worlds");
        String worldName = world.getName();
        World.Environment env = world.getEnvironment();

        for (String name : disabledWorlds) {
            if (worldName.equalsIgnoreCase(name)) return true;
            if (name.equalsIgnoreCase("world_the_end") && env == World.Environment.THE_END) return true;
        }

        return false;
    }

    public boolean hasBypass(Player player) {
        String perm = config.getString("permissions.bypass", "elytradisabled.bypass");
        return player.hasPermission(perm);
    }

    public String getMessage(String key) {
        String message = langConfig.getString(key);
        if (message == null || message.isEmpty()) {
            getLogger().warning("Сообщение с ключом '" + key + "' не найдено в языковом файле!");
            return "[" + key + "]";
        }
        return message.replace('&', '§');
    }

    public boolean shouldPreventEquip() {
        return config.getBoolean("settings.prevent_equip", true);
    }

    public boolean shouldForceUnequip() {
        return config.getBoolean("settings.force_unequip_on_enter", true);
    }

    public boolean shouldPreventGlide() {
        return config.getBoolean("settings.prevent_glide", true);
    }

    public boolean shouldStopGlide() {
        return config.getBoolean("settings.stop_existing_glide", true);
    }

    public boolean shouldPreventFallDamage() {
        return config.getBoolean("settings.prevent_fall_damage", true);
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        setupLanguage();

        if (tickChecker != null) {
            tickChecker.stop();
            tickChecker = null;
        }

        startTickChecker();
    }

    private void startTickChecker() {
        if (config.getBoolean("settings.check_every_tick", true)) {
            tickChecker = new ElytraTickChecker(this);
            tickChecker.start();
        }
    }
}