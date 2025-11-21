package ua.krint.elytraDisabled;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ElytraListener implements Listener {

    private final ElytraDisabled plugin;
    private final java.util.Map<java.util.UUID, Long> messageCooldowns;

    public ElytraListener(ElytraDisabled plugin) {
        this.plugin = plugin;
        this.messageCooldowns = new java.util.HashMap<>();
    }

    public void cleanUpCooldowns() {
        long now = System.currentTimeMillis();
        long cooldown = plugin.getConfig().getLong("settings.message_cooldown", 3000);
        messageCooldowns.entrySet().removeIf(entry -> (now - entry.getValue()) > cooldown * 2);
    }
    private boolean sendMessageWithCooldown(Player p, String message) {
        java.util.UUID uuid = p.getUniqueId();
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
        if (!plugin.shouldPreventEquip()) return;

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        if (e.isShiftClick() && current != null && current.getType() == Material.ELYTRA) {
            if (e.getSlotType() != InventoryType.SlotType.ARMOR) {
                e.setCancelled(true);
                sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
                return;
            }
        }

        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (cursor != null && cursor.getType() == Material.ELYTRA) {
                e.setCancelled(true);
                sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
                return;
            }

            if (current != null && current.getType() == Material.ELYTRA) {
                return;
            }
        }

        if (e.getClick().name().contains("SWAP") && current != null && current.getType() == Material.ELYTRA) {
            e.setCancelled(true);
            sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorEquip(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.shouldPreventEquip()) return;

        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.ELYTRA) {
            if (e.getAction().name().contains("RIGHT")) {
                sendMessageWithCooldown(p, plugin.getMessage("equip_blocked"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.shouldForceUnequip()) return;

        removeElytra(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        World to = e.getTo().getWorld();

        if (plugin.hasBypass(p)) return;
        if (to == null || !plugin.isWorldDisabled(to)) return;
        if (!plugin.shouldForceUnequip()) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline() && plugin.isWorldDisabled(p.getWorld())) {
                removeElytra(p);
            }
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.shouldForceUnequip()) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                removeElytra(p);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(e.getRespawnLocation().getWorld())) return;
        if (!plugin.shouldForceUnequip()) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                removeElytra(p);
            }
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlideToggle(EntityToggleGlideEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;

        Player p = (Player) entity;

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.shouldPreventGlide()) return;

        if (e.isGliding()) {
            e.setCancelled(true);
            p.setGliding(false);
            sendMessageWithCooldown(p, plugin.getMessage("glide_blocked"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFallDamage(EntityDamageEvent e) {
        if (!plugin.shouldPreventFallDamage()) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(e.getEntity() instanceof Player p)) return;

        if (p.hasMetadata("elytra_removed_recently")) {
            e.setCancelled(true);
            p.removeMetadata("elytra_removed_recently", plugin);
        }
    }

    private void removeElytra(Player p) {
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

            p.sendMessage(plugin.getMessage("removed_on_enter"));
        }
    }
}