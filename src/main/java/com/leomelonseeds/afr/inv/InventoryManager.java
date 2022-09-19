package com.leomelonseeds.afr.inv;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class InventoryManager {
private Map<Player, AFRInventory> inventoryCache;
    
    public InventoryManager() {
        inventoryCache = new HashMap<>();
    }
    
    public AFRInventory getInventory(Player player) {
        return inventoryCache.get(player);
    }
    
    // Registers and opens an inventory
    public void registerInventory(Player player, AFRInventory inv) {
        inv.updateInventory();
        player.openInventory(inv.getInventory());
        inventoryCache.put(player, inv);
    }
    
    // Updates all withdraw locations to prevent dupes
    public void updateWithdrawals(String group) {
        for (AFRInventory inv : inventoryCache.values()) {
            if (inv instanceof WithdrawMenu && ((WithdrawMenu) inv).getGroup().equals(group)) {
                inv.updateInventory();
            }
        }
    }
    
    public void removePlayer(Player player) {
        inventoryCache.remove(player);
    }
}
