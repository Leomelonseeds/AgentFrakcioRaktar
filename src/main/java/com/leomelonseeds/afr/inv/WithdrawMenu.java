package com.leomelonseeds.afr.inv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.afr.util.ConfigUtils;

public class WithdrawMenu implements AFRInventory {
    
    private Player player;
    private Inventory inv;
    private String group;
    private Location location;
    private int page;
    private List<StoredItem> items;
    private Map<Integer, StoredItem> currentItems;
    
    public WithdrawMenu(Player player, String group, Location location) {
        this.player = player;
        this.group = group;
        this.location = location;
        this.page = 0;
        currentItems = new HashMap<>();
        
        String title = config.getString("withdraw-menu.title");
        inv = Bukkit.createInventory(null, 54, ConfigUtils.toComponent(title));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        inv.clear();
        
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, ConfigUtils.createItem("fill-item"));
        }
        
        inv.setItem(config.getInt("withdraw-menu.back-item.slot"), ConfigUtils.createItem("withdraw-menu.back-item"));
        
        // Set items
        items = ConfigUtils.stringToItems(location, group);
        int itemSize = items.size();
        double maxPages = Math.ceil(itemSize / 45.0);
        
        // Epic pagination
        if (page > 0) {
            inv.setItem(config.getInt("previous-page.slot"), ConfigUtils.createItem("previous-page"));
        }
        
        if (page < maxPages - 1) {
            inv.setItem(config.getInt("next-page.slot"), ConfigUtils.createItem("next-page"));
        }
        
        // Loops through all available slots, and sets shop items
        for (int i = page * 45; i < Math.min(itemSize, page * 45 + 45); i++) {
            ItemStack item = createWithdrawable(items.get(i));
            currentItems.put(i % 45, items.get(i));
            inv.setItem(i % 45, item);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        if (slot == config.getInt("withdraw-menu.back-item.slot")) {
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
        
        StoredItem storedItem = currentItems.get(slot);
        ItemStack item = new ItemStack(storedItem.getItem());
        int amount;
        
        if (type == ClickType.LEFT) {
            amount = config.getInt("withdraw-menu.left-click-amount");
        } else if (type == ClickType.RIGHT) {
            amount = Math.min(item.getMaxStackSize(), config.getInt("withdraw-menu.right-click-amount"));
        } else if (type == ClickType.SHIFT_LEFT) {
            amount = item.getMaxStackSize();
        } else {
            return;
        }
        
        amount = Math.min(amount, storedItem.getAmount());
        item.setAmount(amount);
        player.getInventory().addItem(item);
        storedItem.addAmount(-1 * amount);
        if (storedItem.getAmount() <= 0) {
            items.remove(storedItem);
        }
        
        ConfigUtils.itemsToString(items, location, group);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
    
    private ItemStack createWithdrawable(StoredItem storedItem) {
        ItemStack item = new ItemStack(storedItem.getItem());
        int amount = storedItem.getAmount();
        ItemMeta meta = item.getItemMeta();
        
        // Name replacements
        String name = config.getString("withdraw-menu.name-format");
        name = name.replace("%amount%", ConfigUtils.formatNumber(amount));
        if (meta.hasDisplayName()) {
            name = name.replace("%name%", ConfigUtils.toPlain(meta.displayName()));
        } else {
            name = "&f" + name.replace("%name%", WordUtils.capitalizeFully(item.getType().toString().toLowerCase().replaceAll("_", " ")));
        }
        
        List<String> format = new ArrayList<>(config.getStringList("withdraw-menu.withdraw-format"));
        for (int i = 0; i < format.size(); i++) {
            String s = format.get(i);
            for (String a : new String[] {"left-click-amount", "right-click-amount"}) {
                s = s.replaceAll("%" + a + "%", config.getInt("withdraw-menu." + a) + "");
            }
            format.set(i, s);
        }
        
        meta.displayName(ConfigUtils.toComponent(name));
        meta.lore(ConfigUtils.toComponent(format));
        item.setItemMeta(meta);
        
        return item;
    }
    
    public String getGroup() {
        return group;
    }
}
