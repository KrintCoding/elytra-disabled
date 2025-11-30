package ua.krint.elytraDisabled;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ElytraDisabled extends JavaPlugin {

    private static final long JOIN_DELAY_TICKS = 5L;
    private static final long TELEPORT_DELAY_TICKS = 2L;
    private static final long RESPAWN_DELAY_TICKS = 2L;
    private static final long COOLDOWN_CLEANUP_INTERVAL = 6000L;

    private FileConfiguration config;
    private FileConfiguration langConfig;
    private ElytraListener listener;
    private ElytraTickChecker tickChecker;
    private UpdateChecker updateChecker;
    private String currentLanguage;

    private String bypassPermission;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        config = getConfig();

        setupLanguage();
        cacheConfigValues();

        listener = new ElytraListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);

        startTickChecker();

        ElytraCommand command = new ElytraCommand(this);
        getCommand("elytra-disabled").setExecutor(command);
        getCommand("elytra-disabled").setTabCompleter(command);

        startCooldownCleanup();

        if (config.getBoolean("settings.check_updates", true)) {
            checkForUpdates();
        }

        getLogger().info("ElytraDisabled enabled successfully!");
        getLogger().info("Language: " + currentLanguage);
        getLogger().info("Protection active in worlds: " + config.getStringList("settings.disable_in_worlds"));
    }

    private void cacheConfigValues() {
        this.bypassPermission = config.getString("permissions.bypass", "elytradisabled.bypass");
    }

    private void checkForUpdates() {
        updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates(hasUpdate -> {
            if (hasUpdate) {
                Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                    @org.bukkit.event.EventHandler
                    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                        Player p = e.getPlayer();
                        if (p.hasPermission("elytradisabled.update.notify")) {
                            Bukkit.getScheduler().runTaskLater(ElytraDisabled.this, () -> {
                                updateChecker.notifyPlayer(p);
                            }, 40L);
                        }
                    }
                }, this);
            }
        });
    }

    private void startCooldownCleanup() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (listener != null) {
                listener.cleanUpCooldowns();
            }
            if (tickChecker != null) {
                tickChecker.cleanUpCooldowns();
            }
        }, COOLDOWN_CLEANUP_INTERVAL, COOLDOWN_CLEANUP_INTERVAL);
    }

    @Override
    public void onDisable() {
        if (tickChecker != null) {
            tickChecker.stop();
        }
        getLogger().info("ElytraDisabled disabled!");
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
            getLogger().warning("Language file '" + currentLanguage + ".yml' not found! Using Russian language.");
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
            getLogger().warning("Failed to load default values for language: " + currentLanguage);
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
        if (world == null) return false;
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
        return player.hasPermission(bypassPermission);
    }

    public String getMessage(String key) {
        String message = langConfig.getString(key);
        if (message == null || message.isEmpty()) {
            getLogger().warning("Message with key '" + key + "' not found in language file!");
            return "[" + key + "]";
        }
        return message.replace('&', '§');
    }

    public void playBlockSound(Player p) {
        if (!config.getBoolean("settings.play_sound", true) || p == null || !p.isOnline()) {
            return;
        }

        try {
            String soundType = config.getString("settings.sound_type", "ENTITY_VILLAGER_NO");
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
            p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid sound type in config: " + config.getString("settings.sound_type"));
            getLogger().warning("Using default sound: ENTITY_VILLAGER_NO");
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    public boolean removeElytra(Player p, String messageKey) {
        if (p == null || !p.isOnline()) {
            return false;
        }

        try {
            PlayerInventory inv = p.getInventory();
            ItemStack chest = inv.getChestplate();

            if (chest != null && chest.getType() == Material.ELYTRA) {
                inv.setChestplate(null);

                if (inv.firstEmpty() != -1) {
                    inv.addItem(chest);
                } else {
                    p.getWorld().dropItemNaturally(p.getLocation(), chest);
                }

                if (p.isGliding()) {
                    p.setGliding(false);
                }

                playBlockSound(p);

                if (messageKey != null && !messageKey.isEmpty()) {
                    p.sendMessage(getMessage(messageKey));
                }

                return true;
            }
        } catch (Exception e) {
            getLogger().warning("Error while removing elytra from player " + p.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        setupLanguage();
        cacheConfigValues();

        if (tickChecker != null) {
            tickChecker.stop();
            tickChecker = null;
        }

        startTickChecker();
        if (config.getBoolean("settings.check_updates", true)) {
            checkForUpdates();
        }
    }

    private void startTickChecker() {
        if (config.getBoolean("settings.check_every_tick", true)) {
            tickChecker = new ElytraTickChecker(this);
            tickChecker.start();
        }
    }

    public long getJoinDelayTicks() {
        return JOIN_DELAY_TICKS;
    }

    public long getTeleportDelayTicks() {
        return TELEPORT_DELAY_TICKS;
    }

    public long getRespawnDelayTicks() {
        return RESPAWN_DELAY_TICKS;
    }
}