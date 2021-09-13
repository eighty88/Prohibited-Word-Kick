package com.github.eighty88.pwk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Prohibited extends JavaPlugin implements Listener {
    public static List<String> words = new ArrayList<>();

    @Override
    public void onEnable() {
        System.out.println("Loading");
        getServer().getPluginManager().registerEvents(this, this);
        final Charset CONFIG_CHARSET = StandardCharsets.UTF_8;
        saveDefaultConfig();
        String confFilePath = getDataFolder() + File.separator + "config.yml";
        try (Reader reader = new InputStreamReader(new FileInputStream(confFilePath), CONFIG_CHARSET)) {
            FileConfiguration config = new YamlConfiguration();
            config.load(reader);
            words = config.getStringList("words");
        } catch (Exception e) {
            onDisable();
        }
    }

    @Override
    public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
        String command = cmd.getName();
        switch (command) {
            case "addwords":
                if(args[0].equals("")) {
                    return false;
                }
                try {
                    words.add(args[0]);
                    FileConfiguration config = getConfig();
                    config.set("words", words);
                    saveConfig();
                    sender.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Prohibited" + ChatColor.AQUA + "]" + ChatColor.BLUE + ": " + ChatColor.AQUA + "禁止ワード " + ChatColor.RED + args[0] + ChatColor.AQUA + " の登録に成功しました。");
                    return true;
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Prohibited" + ChatColor.AQUA + "]" + ChatColor.BLUE + ": " + ChatColor.AQUA + "禁止ワード " + ChatColor.RED + args[0] + ChatColor.AQUA + " の登録に失敗しました。");
                }
            case "wordslist":
                words.stream().map(s -> ChatColor.AQUA + "[" + ChatColor.GREEN + "Prohibited" + ChatColor.AQUA + "]" + ChatColor.BLUE + ": " + ChatColor.AQUA + s).forEach(sender::sendMessage);
                return true;
        }
        return false;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        for(String word:words) {
            if (message.contains(word)) {
                try {
                    System.out.println("[Prohibited]: " + e.getPlayer().getName() + " said " + word + "!");
                    Bukkit.getScheduler().runTaskLater(this, () -> e.getPlayer().kickPlayer(ChatColor.RED + word + "は禁止ワードです!"), 1L);

                    e.setCancelled(true);
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        p.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Prohibited" + ChatColor.AQUA + "]" + ChatColor.BLUE + ": " + ChatColor.RED + e.getPlayer().getName() + ChatColor.AQUA + "は、禁止ワード " + ChatColor.RED + word + ChatColor.AQUA + " を言ったため、キックされました。");
                    }
                } catch (Exception exc) {
                    getLogger().info(e.getPlayer().getName() + " said " + word + " but he/she is OP then can't kick.");
                }
            }
        }
    }
}
