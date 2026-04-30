package com.sakibmc.chaosmobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ChaosMobs extends JavaPlugin implements Listener {

    private final Random random = new Random();
    private FileConfiguration config;
    private boolean vaultEnabled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        // Check for Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultEnabled = true;
            getLogger().info(ChatColor.GREEN + "Vault detected! Mini Boss will reward $1000 on kill.");
        } else {
            getLogger().warning("Vault not found. Mini Boss will not give money rewards.");
        }

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info(ChatColor.GREEN + "ChaosMobs enabled! Automatic events every ~10 minutes.");
        startAutomaticChaosTimer();
    }

    private void startAutomaticChaosTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Bukkit.getOnlinePlayers().isEmpty()) {
                    triggerRandomChaosEvent();
                }
            }
        }.runTaskTimer(this, 20 * 60 * 8, 20 * 60 * 10); // First after 8 min, then every 8-10 min
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
        } else if (roll < config.getInt("mini-boss-chance", 30) + config.getInt("mob-frenzy-chance", 30) + config.getInt("explosive-creepers-chance", 25)) {
            spawnExplosiveCreepers();
        } else {
            spawnGhostGift();
        }
    }

    private void spawn
