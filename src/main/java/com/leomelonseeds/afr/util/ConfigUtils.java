package com.leomelonseeds.afr.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.afr.AgentFrakcioRaktar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ConfigUtils {
    
    // Get group of player, or null if player not in any group
    public static String getGroup(Player player) {
        FileConfiguration config = AgentFrakcioRaktar.getPlugin().getConfig();
        
        for (String g : config.getConfigurationSection("groups").getKeys(false)) {
            String perm = config.getString("groups." + g);
            if (player.hasPermission(perm)) {
                return g;
            }
        }
        
        return null;
    }
    
    // Get the data file for a location
    public static FileConfiguration getData(String configName) {
        // Check if file is there
        File file = new File(AgentFrakcioRaktar.getPlugin().getDataFolder().toString() + "/data", configName);
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        
        return config;
    }
    
    // Create string (filename) from location
    public static String locationToString(Location location) {
        String x = Integer.toString(location.getBlockX());
        String y = Integer.toString(location.getBlockY());
        String z = Integer.toString(location.getBlockZ());
        String world = location.getWorld().toString();
        
        return String.join(",", world, x, y, z) + ".yml";
    }
    
    // Create a location from a string
    public static Location stringToLocation(String s) {
        String args[] = s.split(",");
        World world = Bukkit.getWorld(args[0]);
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        int z = Integer.parseInt(args[3]);
        
        return new Location(world, x, y, z);
    }
    
    // Create and return an item from the config.
    public static ItemStack createItem(String path) {
        FileConfiguration config = AgentFrakcioRaktar.getPlugin().getConfig();
        
        String name = config.getString(path + ".name");
        String material = config.getString(path + ".item");
        
        List<String> lore = config.getStringList(path + ".lore");
        
        // Add cost formats if cost exists
        if (path.contains("shop")) {
            List<String> format = config.getStringList("shop-menu.cost-format");
            int cost = config.getInt(path + ".cost");
            String[] amounts = {"left-click-amount", "right-click-amount", "middle-click-amount"};
            for (String s : format) {
                s = s.replaceAll("%cost%", cost + "");
                for (String a : amounts) {
                    s = s.replaceAll("%" + a + "%", config.getInt("shop-menu." + a) + "");
                }
            }
            lore.addAll(format);
        }
        
        ItemStack item = new ItemStack(Material.getMaterial(material));
        ItemMeta meta = item.getItemMeta();
        
        if (name != null) {
            meta.displayName(toComponent(name));
        }
        
        if (!lore.isEmpty()) {
            meta.lore(toComponent(lore));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Functions for component translation
    public static Component toComponent(String line) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', line));
    }
    
    public static List<Component> toComponent(List<String> lines) {
        List<Component> result = new ArrayList<>();
        for (String s : lines) {
            result.add(toComponent(s));
        }
        return result;
    }

    public static String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}
