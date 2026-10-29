package com.extrask.digitaltokens.listeners;

import com.extrask.digitaltokens.DigitalTokens;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final DigitalTokens plugin;

    public DeathListener(DigitalTokens plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player attacker = victim.getKiller();

        if (attacker == null) return;

        plugin.getTokenManager().addTokens(attacker.getUniqueId(), 1);

        String message = plugin.getConfig().getString("messages.kill_reward",
                "&r | You have gotten 1 &dDigital&r Token!");
        attacker.sendMessage(plugin.getPrefix() + plugin.colorize(message));
        attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
}