package ua.krint.elytraDisabled;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class ElytraListener implements Listener {

    private final ElytraDisabled plugin;

    public ElytraListener(ElytraDisabled plugin) {
        this.plugin = plugin;
    }

    private void warn(Player p, String messageKey) {
        plugin.sendBlockedWarning(p, messageKey);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleGlide(EntityToggleGlideEvent e) {
        if (!e.isGliding()) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player p = (Player) e.getEntity();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.isStopExistingGlide()) return;

        e.setCancelled(true);
        warn(p, "glide_blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (plugin.hasBypass(p)) return;

        if (!plugin.isWorldDisabled(p.getWorld())) return;

        if (!plugin.isPreventEquip()) return;

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        if (e.isShiftClick() && current != null && current.getType() == Material.ELYTRA) {
            if (e.getSlotType() != InventoryType.SlotType.ARMOR) {
                e.setCancelled(true);
                warn(p, "equip_blocked");
                return;
            }
        }

        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (cursor != null && cursor.getType() == Material.ELYTRA) {
                e.setCancelled(true);
                warn(p, "equip_blocked");
                return;
            }

            if (current != null && current.getType() == Material.ELYTRA) {
                return;
            }
        }

        if (e.getClick() == ClickType.NUMBER_KEY && e.getSlotType() == InventoryType.SlotType.ARMOR) {
            int hotbarButton = e.getHotbarButton();
            if (hotbarButton >= 0 && hotbarButton < 9) {
                ItemStack hotbarItem = p.getInventory().getItem(hotbarButton);
                if (hotbarItem != null && hotbarItem.getType() == Material.ELYTRA) {
                    e.setCancelled(true);
                    warn(p, "equip_blocked");
                    return;
                }
            }
        }

        if (e.getClick().name().contains("SWAP") && current != null && current.getType() == Material.ELYTRA) {
            e.setCancelled(true);
            warn(p, "equip_blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.isPreventEquip()) return;

        ItemStack oldCursor = e.getOldCursor();
        if (oldCursor == null || oldCursor.getType() != Material.ELYTRA) return;

        for (int rawSlot : e.getRawSlots()) {
            if (e.getView().getSlotType(rawSlot) == InventoryType.SlotType.ARMOR) {
                e.setCancelled(true);
                warn(p, "equip_blocked");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorEquip(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.isPreventEquip()) return;

        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.ELYTRA) {
            if (e.getAction().name().contains("RIGHT")) {
                warn(p, "equip_blocked");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        if (plugin.hasBypass(p)) return;
        if (!plugin.isWorldDisabled(p.getWorld())) return;
        if (!plugin.isForceUnequipOnEnter()) return;

        plugin.removeElytra(p, "removed_on_enter");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        World to = e.getTo() != null ? e.getTo().getWorld() : null;

        if (plugin.hasBypass(p)) return;
        if (to == null || !plugin.isWorldDisabled(to)) return;
        if (!plugin.isForceUnequipOnEnter()) return;

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
        if (!plugin.isForceUnequipOnEnter()) return;

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
        if (!plugin.isForceUnequipOnEnter()) return;

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
        if (!plugin.isPreventEquip()) return;

        e.setCancelled(true);
        warn(p, "equip_blocked");
    }
}
