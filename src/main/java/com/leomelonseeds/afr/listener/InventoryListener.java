package com.leomelonseeds.afr.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.leomelonseeds.afr.AgentFrakcioRaktar;
import com.leomelonseeds.afr.inv.AFRInventory;
import com.leomelonseeds.afr.inv.AFRMenu;
import com.leomelonseeds.afr.inv.DepositMenu;
import com.leomelonseeds.afr.inv.InventoryManager;
import com.leomelonseeds.afr.util.ConfigUtils;

public class InventoryListener implements Listener {
    
    // Handle clicking a custom inventory
    @EventHandler
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryManager manager = AgentFrakcioRaktar.getPlugin().getInvs();
        
        if (!(manager.getInventory(player) instanceof AFRInventory)) {
            return;
        }
        
        Inventory inv = event.getClickedInventory();
        if (inv == null || !inv.equals(event.getView().getTopInventory())){
            return; 
        }
        
        int slot = event.getSlot();
        
        if (!(manager.getInventory(player) instanceof DepositMenu) || slot >= 45) {
            event.setCancelled(true);
        }

        manager.getInventory(player).registerClick(slot, event.getClick());
    }
    
    // Close a custom inventory and remove it from cache
    @EventHandler
    public void unregisterCustomInventories(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        InventoryManager manager = AgentFrakcioRaktar.getPlugin().getInvs();
        
        // Deposit items if DepositMenu was closed
        if (manager.getInventory(player) instanceof DepositMenu) {
            DepositMenu inv = (DepositMenu) manager.getInventory(player);
            inv.depositItems();
        }
        
        // Unregister custom inventory
        if (manager.getInventory(player) instanceof AFRInventory) {
            manager.removePlayer(player);
        }
    }
    
    // Check opening of custom inventories
    @EventHandler
    public void playerRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Make sure we right clicked a doublechest
        if (event.getClickedBlock().getType() != Material.CHEST) {
            return;
        }
        
        Chest chest = (Chest) event.getClickedBlock().getState();
        InventoryHolder holder = chest.getInventory().getHolder();
        
        if (!(holder instanceof DoubleChest)) {
            return;
        }
        
        DoubleChest doubleChest = (DoubleChest) holder;
        Chest leftChest = (Chest) doubleChest.getLeftSide();
        Chest rightChest = (Chest) doubleChest.getRightSide();
        Location left = leftChest.getLocation();
        Location right = rightChest.getLocation();
        
        // Check against all config locations
        for (String l : AgentFrakcioRaktar.getPlugin().getConfig().getStringList("locations")) {
            Location check = ConfigUtils.stringToLocation(l);

            if (check.equals(left) || check.equals(right)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                String group = ConfigUtils.getGroup(player);

                if (group == null) {
                    ConfigUtils.sendMessage(player, "no-group");
                    return;
                }
                
                new AFRMenu(player, check, group);
                return;
            }
        }
    }
}
