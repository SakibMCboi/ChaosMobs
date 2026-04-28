package com.sakibmc.chaosmobs;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ChaosMobs extends JavaPlugin {

    private final Random random = new Random();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();           // Creates config.yml if it doesn't exist
        config = getConfig();
        
        getLogger().info(ChatColor.GREEN + "ChaosMobs enabled! Random chaos events active.");
        startChaosTimer();
    }

    private void startChaosTimer() {
        int min = config.getInt("event-interval-min", 480);
        int max = config.getInt("event-interval-max", 720);
        int delay = min + random.nextInt(max - min + 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!getServer().getOnlinePlayers().isEmpty()) {
                    triggerRandomChaosEvent();
                }
                // Reschedule with new random time
                int newDelay = min + random.nextInt(max - min + 1);
                this.runTaskLater(ChaosMobs.this, newDelay * 20L);
            }
        }.runTaskLater(this, delay * 20L);
    }

    private void triggerRandomChaosEvent() {
        int total = config.getInt("mini-boss-chance", 30)
                  + config.getInt("mob-frenzy-chance", 30)
                  + config.getInt("explosive-creepers-chance", 25)
                  + config.getInt("ghost-gift-chance", 15);

        int roll = random.nextInt(total);

        if (roll < config.getInt("mini-boss-chance", 30)) {
            spawnMiniBoss();
        } else if (roll < config.getInt("mini-boss-chance", 30) + config.getInt("mob-frenzy-chance", 30)) {
            triggerMobFrenzy();
        } else if (roll < config.getInt("mini-boss-chance", 30) + config.getInt("mob-frenzy-chance", 30) 
                        + config.getInt("explosive-creepers-chance", 25)) {
            spawnExplosiveCreepers();
        } else {
            spawnGhostGift();
        }
    }

    private void spawnMiniBoss() {
        Player player = getRandomPlayer();
        if (player == null) return;

        Location loc = player.getLocation().add(random.nextInt(6) - 3, 1, random.nextInt(6) - 3);
        Zombie boss = player.getWorld().spawn(loc, Zombie.class);

        String name = config.getString("mini-boss-name", "☠ Chaos Overlord");
        boss.setCustomName(ChatColor.RED + name);
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(config.getInt("mini-boss-health", 120));
        boss.setHealth(config.getInt("mini-boss-health", 120));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 1));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));

        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        broadcastEvent("☠ " + name + " has spawned near " + player.getName() + "!");
    }

    private void triggerMobFrenzy() {
        Player player = getRandomPlayer();
        if (player == null) return;

        int radius = config.getInt("mob-frenzy-radius", 30);
        int duration = config.getInt("mob-frenzy-duration", 45) * 20;

        player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius).forEach(entity -> {
            if (entity instanceof Monster mob && !(entity instanceof Boss)) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1));
            }
        });

        broadcastEvent("⚡ Mob Frenzy! Nearby monsters are enraged for " + (duration/20) + " seconds!");
    }

    private void spawnExplosiveCreepers() {
        Player player = getRandomPlayer();
        if (player == null) return;

        int count = config.getInt("explosive-creeper-count", 3);
        int fuse = config.getInt("explosive-creeper-fuse", 30);

        for (int i = 0; i < count; i++) {
            Location loc = player.getLocation().add(random.nextInt(8) - 4, 1, random.nextInt(8) - 4);
            Creeper creeper = player.getWorld().spawn(loc, Creeper.class);
            creeper.setPowered(true);
            creeper.setMaxFuseTicks(fuse);
        }

        broadcastEvent("💥 " + count + " Supercharged Creepers are hunting! Run!");
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

        int delay = config.getInt("ghost-gift-duration", 15) * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ghost.isValid()) {
                    ghost.getWorld().dropItemNaturally(ghost.getLocation(), 
                        new org.bukkit.inventory.ItemStack(Material.EMERALD, config.getInt("ghost-emerald-amount", 3)));
                    ghost.getWorld().dropItemNaturally(ghost.getLocation(), 
                        new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, config.getInt("ghost-gold-amount", 2)));
                    ghost.remove();
                }
            }
        }.runTaskLater(this, delay);
        
        broadcastEvent("✨ A Friendly Ghost appeared near " + player.getName() + "! Gifts incoming in " + (delay/20) + " seconds.");
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
