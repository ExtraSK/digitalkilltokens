package com.extrask.digitaltokens.commands;

import com.extrask.digitaltokens.DigitalTokens;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class TokensCommand implements CommandExecutor, TabCompleter {

    private final DigitalTokens plugin;

    public TokensCommand(DigitalTokens plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can check a token balance.");
                return true;
            }

            int balance = plugin.getTokenManager().getTokens(player.getUniqueId());
            player.sendMessage(plugin.getPrefix() + plugin.colorize(
                    plugin.getConfig().getString("messages.balance",
                            "&7 | &c%balance% &rᴛᴏᴋᴇɴѕ")
                            .replace("%balance%", String.valueOf(balance))));
            return true;
        }

        if (args[0].equalsIgnoreCase("withdraw")) {
            withdraw(sender, args);
            return true;
        }

        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("digitaltokens.admin")) {
                sender.sendMessage(plugin.colorize("&cYou do not have permission to use this command."));
                return true;
            }
            modifyTokens(sender, args, args[0].equalsIgnoreCase("add"));
            return true;
        }

        sendUsage(sender);
        return true;
    }

    private void withdraw(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can withdraw tokens.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + plugin.colorize(
                    plugin.getConfig().getString("messages.withdraw_usage",
                            "&7 | ᴜѕᴀɢᴇ: &c/tokens withdraw &r(amount)")));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                player.sendMessage(plugin.getPrefix() + "&cAmount must be positive!");
                return;
            }
        } catch (NumberFormatException exception) {
            player.sendMessage(plugin.getPrefix() + "&cInvalid amount!");
            return;
        }

        if (!plugin.getTokenManager().removeTokens(player.getUniqueId(), amount)) {
            player.sendMessage(plugin.getPrefix() + plugin.colorize(
                    plugin.getConfig().getString("messages.insufficient_funds",
                            "&7 | &fʏᴏᴜ ʜᴀᴠᴇ &cɪɴѕᴜꜰꜰɪᴄɪᴇɴᴛ ꜰᴜɴᴅѕ")));
            return;
        }

        Material material = Material.getMaterial(plugin.getConfig()
                .getString("item.material", "NETHER_STAR").toUpperCase());
        if (material == null) material = Material.NETHER_STAR;

        String itemName = plugin.getConfig().getString("item.name", "&c&lᴋɪʟʟ ᴛᴏᴋᴇɴ");
        List<String> loreLines = plugin.getConfig().getStringList("item.lore");
        if (loreLines.isEmpty()) {
            loreLines = List.of("&8ᴇᴀʀɴᴇᴅ ʙʏ ᴋɪʟʟɪɴɢ ᴘʟᴀʏᴇʀs");
        }

        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack item = new ItemStack(material, stackSize);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(plugin.colorize(itemName));
            List<String> coloredLore = new ArrayList<>();
            for (String line : loreLines) {
                coloredLore.add(plugin.colorize(line));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
            remaining -= stackSize;
        }

        player.sendMessage(plugin.getPrefix() + plugin.colorize(
                plugin.getConfig().getString("messages.withdraw_success",
                        "&7 | ʏᴏᴜ ᴡɪᴛʜᴅʀᴇᴡ &d%amount% &rᴋɪʟʟ ᴛᴏᴋᴇɴѕ")
                        .replace("%amount%", String.valueOf(amount))));
    }

    private void modifyTokens(CommandSender sender, String[] args, boolean add) {
        if (args.length != 3) {
            sender.sendMessage(plugin.colorize(add
                    ? "&cUsage: /tokens add <player> <amount>"
                    : "&cUsage: /tokens remove <player> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.colorize("&cThat player is not online."));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(plugin.colorize("&cAmount must be a whole number."));
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(plugin.colorize("&cAmount must be positive."));
            return;
        }

        boolean successful = add
                ? addTokens(target, amount)
                : plugin.getTokenManager().removeTokens(target.getUniqueId(), amount);
        if (!successful) {
            sender.sendMessage(plugin.colorize("&cThat player does not have enough tokens."));
            return;
        }

        String messageKey = add ? "messages.admin_added" : "messages.admin_removed";
        String defaultMessage = add
                ? "&c&lDIGITAL TOKENS &r&7| Gave %amount% Tokens to %arg-1%"
                : "&c&lDIGITAL TOKENS &r&7| Removed %amount% Tokens from %arg-1%";
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString(messageKey, defaultMessage)
                .replace("%amount%", String.valueOf(amount))
                .replace("%arg-1%", target.getName())));
    }

    private boolean addTokens(Player target, int amount) {
        plugin.getTokenManager().addTokens(target.getUniqueId(), amount);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&cUsage: /tokens [withdraw <amount>|add <player> <amount>|remove <player> <amount>]"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        boolean admin = sender.hasPermission("digitaltokens.admin");
        if (args.length == 1) {
            completions.add("withdraw");
            if (admin) {
                completions.add("add");
                completions.add("remove");
            }
        } else if (args.length == 2 && admin
                && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(value -> !value.toLowerCase().startsWith(input));
        return completions;
    }
}