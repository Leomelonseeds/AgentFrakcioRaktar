package com.leomelonseeds.afr.inv;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.afr.util.ConfigUtils;

public class AFRMenu implements AFRInventory {
    
    private Player player;
    private Inventory inv;
    private String group;
    private Location location;
    
    public AFRMenu(Player player, Location location, String group) {
        this.player = player;
        this.location = location;
        this.group = group;
        
        String title = config.getString("main-menu.title");
        inv = Bukkit.createInventory(null, 27, ConfigUtils.toComponent(title));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, ConfigUtils.createItem("fill-item"));
        }
        
        for (String i : new String[] {"deposit", "withdraw", "shop"}) {
            String path = "main-menu." + i;
            ItemStack item = ConfigUtils.createItem(path);
            inv.setItem(config.getInt(path + ".slot"), item);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        if (group == null) {
            ConfigUtils.sendMessage(player, "no-group");
            return;
        }
        
        if (slot == config.getInt("main-menu.deposit.slot")) {
            new DepositMenu(player, group, location);
            return;
        }
        
        if (slot == config.getInt("main-menu.withdraw.slot")) {
            new WithdrawMenu(player, group, location);
            return;
        }
        
        if (slot == config.getInt("main-menu.shop.slot")) {
            new ShopMenu(player, group, location);
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
