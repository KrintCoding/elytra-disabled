package ua.krint.elytraDisabled;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
        for (Player p : Bukkit.getOnlinePlayers()) {

            if (Bukkit.getOnlinePlayers().isEmpty()) return;

            if (plugin.hasBypass(p)) continue;

            if (!plugin.isWorldDisabled(p.getWorld())) continue;

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

                p.setMetadata("elytra_removed_recently",
                        new org.bukkit.metadata.FixedMetadataValue(plugin, true));

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (p.isOnline()) {
                        p.removeMetadata("elytra_removed_recently", plugin);
                    }
                }, 60L);
            }

            if (p.isGliding()) {
                p.setGliding(false);

                sendMessageWithCooldown(p, plugin.getMessage("glide_blocked"));
            }
        }
    }
}