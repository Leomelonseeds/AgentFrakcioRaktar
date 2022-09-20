package com.leomelonseeds.afr.inv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.afr.AgentFrakcioRaktar;
import com.leomelonseeds.afr.util.ConfigUtils;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class ShopMenu implements AFRInventory {
    
    private Player player;
    private Inventory inv;
    private String group;
    private int page;
    private Location location;
    
    public ShopMenu(Player player, String group, Location location) {
        this.player = player;
        this.group = group;
        this.page = 0;
        this.location = location;
        
        String title = config.getString("shop-menu.title");
        inv = Bukkit.createInventory(null, 54, ConfigUtils.toComponent(title));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        inv.clear();
        
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, ConfigUtils.createItem("fill-item"));
        }
        
        inv.setItem(config.getInt("shop-menu.back-item.slot"), ConfigUtils.createItem("shop-menu.back-item"));
        
        Set<String> keys = config.getConfigurationSection("shop-menu.shops." + group).getKeys(false);
        List<String> keyList = new ArrayList<>(keys);
        int keySize = keyList.size();
        double maxPages = Math.ceil(keySize / 45.0);
        
        // Epic pagination
        if (page > 0) {
            inv.setItem(config.getInt("previous-page.slot"), ConfigUtils.createItem("previous-page"));
        }
        
        if (page < maxPages - 1) {
            inv.setItem(config.getInt("next-page.slot"), ConfigUtils.createItem("next-page"));
        }
        
        // Loops through all available slots, and sets shop items
        for (int i = page * 45; i < Math.min(keySize, page * 45 + 45); i++) {
            ItemStack item = ConfigUtils.createItem("shop-menu.shops." + group + "." + keyList.get(i));
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(AgentFrakcioRaktar.getPlugin(), "cost"),
                    PersistentDataType.DOUBLE, config.getDouble("shop-menu.shops." + group + "." + keyList.get(i) + ".cost"));
            item.setItemMeta(meta);
            inv.setItem(i % 45, item);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        if (slot == config.getInt("shop-menu.back-item.slot")) {
            new AFRMenu(player, location, group);
            return;
        }
        
        if (slot == config.getInt("previous-page.slot") && 
                inv.getItem(slot).getType() == Material.getMaterial(config.getString("previous-page.item"))) {
            page--;
            updateInventory();
            return;
        }
        
        if (slot == config.getInt("next-page.slot") && 
                inv.getItem(slot).getType() == Material.getMaterial(config.getString("next-page.item"))) {
            page++;
            updateInventory();
            return;
        }
        
        if (slot >= 45 || inv.getItem(slot) == null) {
            return;
        }
        
        if (player.getInventory().firstEmpty() == -1) {
            ConfigUtils.sendMessage(player, "inventory-full");
            return;
        }
        
        ItemStack item = inv.getItem(slot);
        double cost = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(
                AgentFrakcioRaktar.getPlugin(), "cost"), PersistentDataType.DOUBLE);
        Economy econ = AgentFrakcioRaktar.getPlugin().getEconomy();
        int amount;
        
        if (type == ClickType.LEFT) {
            amount = config.getInt("shop-menu.left-click-amount");
        } else if (type == ClickType.RIGHT) {
            amount = config.getInt("shop-menu.right-click-amount");
        } else if (type == ClickType.SHIFT_LEFT) {
            amount = item.getMaxStackSize();
        } else {
            return;
        }
        
        double finalCost = amount * cost;
        if (econ.getBalance(player) < finalCost) {
            String message = config.getString("messages.not-enough-money").replace("%cost%", finalCost + "");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }
        
        player.getInventory().addItem(new ItemStack(item.getType(), amount));
        econ.withdrawPlayer(player, finalCost);
        String message = config.getString("messages.purchase-successful");
        message = message.replaceAll("%amount%", amount + "");
        message = message.replaceAll("%cost%", finalCost + "");
        message = message.replaceAll("%name%", WordUtils.capitalizeFully(item.getType().toString().toLowerCase().replaceAll("_", " ")));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
