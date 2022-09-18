package com.leomelonseeds.afr.inv;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class AFRMenu implements AFRInventory {
    
    private Player player;
    private Inventory inv;
    
    public AFRMenu(Player player) {
        this.player = player;
    }

    @Override
    public void updateInventory() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
