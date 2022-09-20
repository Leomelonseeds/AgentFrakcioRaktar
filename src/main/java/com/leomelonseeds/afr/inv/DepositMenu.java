package com.leomelonseeds.afr.inv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.afr.util.ConfigUtils;

import net.md_5.bungee.api.ChatColor;

public class DepositMenu implements AFRInventory {

    private Player player;
    private Inventory inv;
    private String group;
    private Location location;
    
    public DepositMenu(Player player, String group, Location location) {
        this.player = player;
        this.group = group;
        this.location = location;
        
        String title = config.getString("deposit-menu.title");
        inv = Bukkit.createInventory(null, 54, ConfigUtils.toComponent(title));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, ConfigUtils.createItem("fill-item"));
        }
        
        for (String s : new String[] {"back-item", "confirm"}) {
            inv.setItem(config.getInt("deposit-menu." + s + ".slot"), ConfigUtils.createItem("deposit-menu." + s));
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        if (slot == config.getInt("deposit-menu.back-item.slot")) {
            new AFRMenu(player, location, group);
            return;
        }
        
        if (type != ClickType.LEFT) {
            return;
        }
        
        if (slot == config.getInt("deposit-menu.confirm.slot")) {
            depositItems(true);
        }
    }
    
    public void depositItems(Boolean async) {
        int count = 0;
        // Collect all deposited items
        List<ItemStack> toDeposit = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                toDeposit.add(item);
                count += item.getAmount();
            }
            inv.setItem(i, new ItemStack(Material.AIR));
        }
        
        // Make sure its not empty.
        if (toDeposit.isEmpty()) {
            return;
        }
        
        String message = config.getString("messages.items-deposited").replace("%amount%", count + "");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        if (async) {
            ConfigUtils.updateStringAsync(toDeposit, location, group);
        } else {
            ConfigUtils.updateString(toDeposit, location, group);
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
