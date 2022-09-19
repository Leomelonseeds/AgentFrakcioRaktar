package com.leomelonseeds.afr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.leomelonseeds.afr.AgentFrakcioRaktar;
import com.leomelonseeds.afr.inv.StoredItem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ConfigUtils {
    
    // Turn a list of StoredItem into a string, and store it in the file
    public static void itemsToString(List<StoredItem> list, Location location, String group) {
        String output = "";
        try {
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(str);
            
            data.writeInt(list.size());
            for (StoredItem i : list) {
                data.writeObject(i);
            }
            
            output = Base64.getEncoder().encodeToString(str.toByteArray());
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to save a StoredItem string to the config");
        }
        
        FileConfiguration storage = getData(locationToString(location));
        storage.set(group, output);
    }
    
    // Fetch string from input and turn it into list of StoredItem
    public static List<StoredItem> stringToItems(Location location, String group) {
        String input = getData(locationToString(location)).getString(group);
        List<StoredItem> output = new ArrayList<>();
        
        if (input.isEmpty()) {
            return output;
        }

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(input));
            BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            
            int size = data.readInt();
            for (int i = 0; i < size; i++) {
                StoredItem invItem = (StoredItem) data.readObject();
                output.add(invItem);
            }
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to read a StoredItem string from the config");
            e.printStackTrace();
        }
        
        return output;
    }
    
    // Update a string when new items are deposited
    public static void updateString(List<ItemStack> input, Location location, String group) {
        Bukkit.getScheduler().runTaskAsynchronously(AgentFrakcioRaktar.getPlugin(), () -> {
            List<StoredItem> currentItems = stringToItems(location, group);
            List<ItemStack> iterate = new ArrayList<>(input);
            // Add duplicate items to list
            for (StoredItem s : currentItems) {
                ItemStack item = s.getItem();
                for (ItemStack t : iterate) {
                    if (t.isSimilar(item)) {
                        s.addAmount(t.getAmount());
                        input.remove(t);
                    }
                }
            }
            
            // The rest are appended to the StoredItems list
            // To avoid CME, make set of items that have already been added
            Set<ItemStack> added = new HashSet<>();
            for (ItemStack item : input) {
                if (added.contains(item)) {
                    continue;
                }
                StoredItem si = new StoredItem(item, 0);
                for (ItemStack jtem : input) {
                    if (jtem.isSimilar(item)) {
                        si.addAmount(jtem.getAmount());
                    }
                }
                currentItems.add(si);
            }
            
            // Put back in storage
            itemsToString(currentItems, location, group);
        });
    }
    
    public static void sendMessage(Player player, String message) {
        FileConfiguration config = AgentFrakcioRaktar.getPlugin().getConfig();
        String path = "messages." + message;
        String result = config.getString(path);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', result));
    }
    
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
        String world = location.getWorld().getName();
        
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
        if (config.contains(path + ".cost")) {
            List<String> format = config.getStringList("shop-menu.cost-format");
            int cost = config.getInt(path + ".cost");
            for (String s : format) {
                s = s.replaceAll("%cost%", cost + "");
                for (String a : new String[] {"left-click-amount", "right-click-amount"}) {
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
