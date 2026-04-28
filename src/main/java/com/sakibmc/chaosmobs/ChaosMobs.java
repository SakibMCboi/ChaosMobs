package com.sakibmc.chaosmobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ChaosMobs extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "ChaosMobs has been enabled! Try /chaos");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "ChaosMobs has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chaos")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }

            // Spawn a mini boss near the player
            Zombie boss = player.getWorld().spawn(player.getLocation().add(3, 0, 3), Zombie.class);
            boss.setCustomName(ChatColor.RED + "Mini Chaos Boss");
            boss.setCustomNameVisible(true);
            boss.setMaxHealth(80);
            boss.setHealth(80);
            boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 1));
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));

            player.sendMessage(ChatColor.RED + "☠ A Mini Chaos Boss has spawned nearby!");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "⚡ " + player.getName() + " triggered a chaos event!");

            return true;
        }
        return false;
    }
}
