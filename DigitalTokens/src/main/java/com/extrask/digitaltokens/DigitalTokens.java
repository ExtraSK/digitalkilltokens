package com.extrask.digitaltokens;

import com.extrask.digitaltokens.commands.TokensCommand;
import com.extrask.digitaltokens.listeners.DeathListener;
import com.extrask.digitaltokens.manager.TokenManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class DigitalTokens extends JavaPlugin {

    private static DigitalTokens instance;
    private TokenManager tokenManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        tokenManager = new TokenManager(this);
        tokenManager.loadData();

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

        TokensCommand tokensCommand = new TokensCommand(this);
        getCommand("tokens").setExecutor(tokensCommand);
        getCommand("tokens").setTabCompleter(tokensCommand);

        getLogger().info("DigitalTokens v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (tokenManager != null) {
            tokenManager.saveData();
        }
        getLogger().info("DigitalTokens disabled.");
    }

    public static DigitalTokens getInstance() {
        return instance;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getPrefix() {
        return colorize(getConfig().getString("prefix", "&c&lDigital Kill Tokens"));
    }
}