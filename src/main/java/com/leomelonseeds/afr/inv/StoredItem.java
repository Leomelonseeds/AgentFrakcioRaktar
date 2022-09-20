package com.leomelonseeds.afr.inv;

import java.io.Serializable;

import org.bukkit.inventory.ItemStack;

public class StoredItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private ItemStack item;
    private int amount;
    
    public StoredItem(ItemStack item, int amount) {
        this.item = item;
        this.amount = amount;
        
        // Safety check
        item.setAmount(1);
    }
    
    public void addAmount(int i) {
        amount = amount + i;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public ItemStack getItem() {
        return item;
    }
}
