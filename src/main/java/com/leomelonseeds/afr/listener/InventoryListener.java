package com.leomelonseeds.afr.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.afr.AgentFrakcioRaktar;
import com.leomelonseeds.afr.inv.AFRInventory;
import com.leomelonseeds.afr.inv.InventoryManager;

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

        manager.getInventory(player).registerClick(event.getSlot(), event.getClick());
    }
    
    // Close a custom inventory and remove it from cache
    @EventHandler
    public void unregisterCustomInventories(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        
        // Unregister
        InventoryManager manager = AgentFrakcioRaktar.getPlugin().getInvs();
        if (manager.getInventory(player) instanceof AFRInventory) {
            manager.removePlayer(player);
        }
    }
}
