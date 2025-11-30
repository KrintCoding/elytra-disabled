package ua.krint.elytraDisabled;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraTickChecker {

    private final ElytraDisabled plugin;
    private BukkitTask task;
    private final Map<UUID, Long> messageCooldowns;

    public ElytraTickChecker(ElytraDisabled plugin) {
        this.plugin = plugin;
        this.messageCooldowns = new HashMap<>();
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::check, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public void cleanUpCooldowns() {
        long now = System.currentTimeMillis();
        long cooldown = plugin.getConfig().getLong("settings.message_cooldown", 3000);
        messageCooldowns.entrySet().removeIf(entry -> (now - entry.getValue()) > cooldown * 2);
    }

    private boolean sendMessageWithCooldown(Player p, String message) {
        UUID uuid = p.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldown = plugin.getConfig().getLong("settings.message_cooldown", 3000);

        Long lastMessage = messageCooldowns.get(uuid);
        if (lastMessage != null && (now - lastMessage) < cooldown) {
            return false;
        }

        messageCooldowns.put(uuid, now);
        p.sendMessage(message);
        return true;
    }

    private void check() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.hasBypass(p)) continue;
            if (!plugin.isWorldDisabled(p.getWorld())) continue;

            boolean forceUnequip = plugin.getConfig().getBoolean("settings.force_unequip_on_enter", true);
            boolean stopGlide = plugin.getConfig().getBoolean("settings.stop_existing_glide", true);

            if (forceUnequip) {
                ItemStack chestplate = p.getInventory().getChestplate();
                if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
                    plugin.removeElytra(p, null);
                }
            }

            if (stopGlide && p.isGliding()) {
                p.setGliding(false);
                if (!forceUnequip) {
                    sendMessageWithCooldown(p, plugin.getMessage("glide_blocked"));
                }
            }
        }
    }
}