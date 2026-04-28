package com.sakibmc.chaosmobs;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ChaosMobs extends JavaPlugin {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "ChaosMobs has been enabled! Random chaos events will start soon.");
        
        // Start random chaos events
        startChaosTimer();
    }

    private void startChaosTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!getServer().getOnlinePlayers().isEmpty()) {
                    triggerRandomChaosEvent();
                }
            }
        }.runTaskTimer(this, 20 * 60 * 8, 20 * 60 * 10); // Starts after 8 minutes, then every 8-10 minutes
    }

    private void triggerRandomChaosEvent() {
        int event = random.nextInt(4); // 0 to 3

        switch (event) {
            case 0 -> spawnMiniBoss();
            case 1 -> triggerMobFrenzy();
            case 2 -> spawnExplosiveCreepers();
            case 3 -> spawnGhostGift();
        }
    }

    private void spawnMiniBoss() {
        Player player = getRandomPlayer();
        if (player == null) return;

        Location loc = player.getLocation().add(random.nextInt(6) - 3, 1, random.nextInt(6) - 3);
        Zombie boss = player.getWorld().spawn(loc, Zombie.class);

        boss.setCustomName(ChatColor.RED + "☠ Chaos Overlord");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(120);
        boss.setHealth(120);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 1));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));

        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        broadcastEvent("☠ A powerful Chaos Overlord has spawned near " + player.getName() + "!");
    }

    private void triggerMobFrenzy() {
        Player player = getRandomPlayer();
        if (player == null) return;

        player.getWorld().getNearbyEntities(player.getLocation(), 30, 30, 30).forEach(entity -> {
            if (entity instanceof Monster mob && !(entity instanceof Boss)) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 45, 1));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 45, 1));
            }
        });

        broadcastEvent("⚡ Mob Frenzy! Nearby monsters are enraged for 45 seconds!");
    }

    private void spawnExplosiveCreepers() {
        Player player = getRandomPlayer();
        if (player == null) return;

        for (int i = 0; i < 3; i++) {
            Location loc = player.getLocation().add(random.nextInt(8) - 4, 1, random.nextInt(8) - 4);
            Creeper creeper = player.getWorld().spawn(loc, Creeper.class);
            creeper.setPowered(true);
            creeper.setMaxFuseTicks(30); // Very fast fuse
        }

        broadcastEvent("💥 Supercharged Creepers are hunting! Run!");
    }

    private void spawnGhostGift() {
        Player player = getRandomPlayer();
        if (player == null) return;

        Location loc = player.getLocation().add(0, 2, 0);
        Zombie ghost = player.getWorld().spawn(loc, Zombie.class);

        ghost.setCustomName(ChatColor.AQUA + "✨ Friendly Ghost");
        ghost.setCustomNameVisible(true);
        ghost.setBaby();
        ghost.setInvisible(true);
        ghost.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 999999, 0));

        // Drop gifts after 15 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ghost.isValid()) {
                    ghost.getWorld().dropItemNaturally(ghost.getLocation(), new org.bukkit.inventory.ItemStack(Material.EMERALD, 3));
                    ghost.getWorld().dropItemNaturally(ghost.getLocation(), new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 2));
                    ghost.remove();
                }
            }
        }.runTaskLater(this, 20 * 15);
        
        broadcastEvent("✨ A Friendly Ghost appeared near " + player.getName() + "! It will give gifts soon.");
    }

    private Player getRandomPlayer() {
        var players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return null;
        return players.stream().skip(random.nextInt(players.size())).findFirst().orElse(null);
    }

    private void broadcastEvent(String message) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Chaos] " + ChatColor.YELLOW + message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chaos")) {
            triggerRandomChaosEvent();
            sender.sendMessage(ChatColor.GREEN + "Random chaos event triggered!");
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "ChaosMobs has been disabled.");
    }
}
