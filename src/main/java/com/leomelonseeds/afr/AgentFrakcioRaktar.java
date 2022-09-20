package com.leomelonseeds.afr;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.leomelonseeds.afr.command.AFRCommand;
import com.leomelonseeds.afr.inv.InventoryManager;
import com.leomelonseeds.afr.listener.InventoryListener;
import com.leomelonseeds.afr.util.ConfigUtils;

import net.milkbowl.vault.economy.Economy;

public class AgentFrakcioRaktar extends JavaPlugin {
    
    private static AgentFrakcioRaktar plugin;
    private InventoryManager invManager;
    private static Economy econ;
    
    @Override
    public void onEnable() {
        
        log("Enabling AgentFrakcioRaktar...");
        // Register plugin instance
        plugin = this;
        
        // Setup files
        log("Setting up data and config files...");
        setupFiles();
        log("File setup complete.");
        
        // Load commands and events
        log("Loading commands and events...");
        getCommand("afr").setExecutor(new AFRCommand());
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        log("Commands and events loaded.");
        
        log("Initializing vault integration");
        setupVault();
        log("Economy setup complete.");
        
        log("Initializing inventory manager");
        this.invManager = new InventoryManager();
        log("Initialized inventory manager.");
    }
    
    @Override
    public void onDisable() {
        log("Closing and auto-depositing all inventories...");
        invManager.depositAll();
        log("All inventories closed.");
        
        log("Disabling AgentFrakcioRaktar...");
    }
    
    // For each file, verify that it has the correct contents.
    private void setupFiles() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        
        // Make /data directory if not exists
        File dataFolder = new File(getDataFolder().toString() + "/data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        
        // We need 1 file for each location
        for (String l : config.getStringList("locations")) {
            // Add missing files
            File file = new File(getDataFolder().toString() + "/data", l + ".yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    log("Created a file for location " + l);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "[AFR] Could not create a file for location " + l);
                    e.printStackTrace();
                }
            }
            
            // Attempt to load each file
            FileConfiguration locationFile = ConfigUtils.getData(l + ".yml");
            
            // Fills file with configuration sections representing groups
            for (String g : config.getConfigurationSection("groups").getKeys(false)) {
                if (!locationFile.contains(g)) {
                    locationFile.set(g, "");
                }
            }
            
            // Save file back to location
            try {
                locationFile.save(file);
                log("Loaded file for location " + l);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "[AFR] Could not load a file for location " + l);
                e.printStackTrace();
            }
        }
    }
    
    // Get plugin instance
    public static AgentFrakcioRaktar getPlugin() {
        return plugin;
    }
    
    // Setup Economy
    private void setupVault() {
        RegisteredServiceProvider<Economy> rspE = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rspE.getProvider();
    }
    
    // Get inv manager
    public InventoryManager getInvs() {
        return invManager;
    }
    
    // Get economy
    public Economy getEconomy() {
        return econ;
    }
    
    // Log a message
    public void log(String m) {
        Bukkit.getLogger().log(Level.INFO, "[AFR] " + m);
    }
}
