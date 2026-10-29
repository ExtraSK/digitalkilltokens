package com.extrask.digitaltokens.manager;

import com.extrask.digitaltokens.DigitalTokens;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenManager {

    private final DigitalTokens plugin;
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private File dataFile;
    private YamlConfiguration data;

    public TokenManager(DigitalTokens plugin) {
        this.plugin = plugin;
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                tokens.put(uuid, data.getInt(key, 0));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveData() {
        if (dataFile == null) return;
        for (Map.Entry<UUID, Integer> entry : tokens.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save token data: " + e.getMessage());
        }
    }

    public int getTokens(UUID uuid) {
        return tokens.getOrDefault(uuid, 0);
    }

    public void setTokens(UUID uuid, int amount) {
        tokens.put(uuid, Math.max(0, amount));
    }

    public void addTokens(UUID uuid, int amount) {
        tokens.merge(uuid, amount, Integer::sum);
    }

    public boolean removeTokens(UUID uuid, int amount) {
        int current = getTokens(uuid);
        if (current < amount) return false;
        tokens.put(uuid, current - amount);
        return true;
    }
}