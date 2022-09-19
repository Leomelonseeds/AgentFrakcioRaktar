package com.leomelonseeds.afr.inv;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.afr.AgentFrakcioRaktar;

public interface AFRInventory {
    InventoryManager manager = AgentFrakcioRaktar.getPlugin().getInvs();
    FileConfiguration config = AgentFrakcioRaktar.getPlugin().getConfig();
    
    public void updateInventory();
    
    public void registerClick(int slot, ClickType type);
    
    public Inventory getInventory();
}
