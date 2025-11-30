package ua.krint.elytraDisabled;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraListener implements Listener {

    private final ElytraDisabled plugin;
    private final Map<UUID, Long> messageCooldowns;

    public ElytraListener(ElytraDisabled plugin) {
        this.plugin = plugin;
        this.messageCooldowns = new HashMap<>();
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (plugin.hasBypass(p)) return;

        if (!plugin.isWorldDisabled(p.getWorld())) return;

        if (!plugin.getConfig().getBoolean("settings.prevent_equip", true)) return;

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        if (e.isShiftClick() && current != null && current.getType() == Material.ELYTRA) {
            if (e.getSlotType() != InventoryType.SlotType.ARMOR) {
                e.setCancelled(true);
                sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
                plugin.playBlockSound(p);
                return;
            }
        }

        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (cursor != null && cursor.getType() == Material.ELYTRA) {
                e.setCancelled(true);
                sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
                plugin.playBlockSound(p);
                return;
            }

            if (current != null && current.getType() == Material.ELYTRA) {
                return;
            }
        }

        if (e.getClick().name().contains("SWAP") && current != null && current.getType() == Material.ELYTRA) {
            e.setCancelled(true);
            sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
            plugin.playBlockSound(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorEquip(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.getConfig().getBoolean("settings.prevent_equip", true)) return;

        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.ELYTRA) {
            if (e.getAction().name().contains("RIGHT")) {
                sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
                plugin.playBlockSound(p);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.getConfig().getBoolean("settings.force_unequip_on_enter", true)) return;

        plugin.removeElytra(p, "removed_on_enter");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        World to = e.getTo() != null ? e.getTo().getWorld() : null;

        if (plugin.hasBypass(p)) return;
        if (to == null || !plugin.isWorldDisabled(to)) return;
        if (!plugin.getConfig().getBoolean("settings.force_unequip_on_enter", true)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline() && plugin.isWorldDisabled(p.getWorld())) {
                plugin.removeElytra(p, "removed_on_enter");
            }
        }, plugin.getTeleportDelayTicks());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.getConfig().getBoolean("settings.force_unequip_on_enter", true)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                plugin.removeElytra(p, "removed_on_enter");
            }
        }, plugin.getJoinDelayTicks());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(e.getRespawnLocation().getWorld())) return;
        if (!plugin.getConfig().getBoolean("settings.force_unequip_on_enter", true)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                plugin.removeElytra(p, "removed_on_enter");
            }
        }, plugin.getRespawnDelayTicks());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispenseArmor(org.bukkit.event.block.BlockDispenseArmorEvent e) {
        if (e.getItem().getType() != Material.ELYTRA) return;
        if (!(e.getTargetEntity() instanceof Player)) return;

        Player p = (Player) e.getTargetEntity();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.getConfig().getBoolean("settings.prevent_equip", true)) return;

        e.setCancelled(true);
        sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
        plugin.playBlockSound(p);
    }
}