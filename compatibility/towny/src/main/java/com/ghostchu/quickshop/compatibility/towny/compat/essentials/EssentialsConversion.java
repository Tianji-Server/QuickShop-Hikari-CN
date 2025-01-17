package com.ghostchu.quickshop.compatibility.towny.compat.essentials;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.compatibility.towny.EssStringUtil;
import com.ghostchu.quickshop.compatibility.towny.Main;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EssentialsConversion implements UuidConversion {
    @Override
    public UUID convertTownyAccount(Town town) {
        String vaultAccountName = processAccount(town.getAccount().getName());
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + vaultAccountName).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public UUID convertTownyAccount(Nation nation) {
        String vaultAccountName = processAccount(nation.getAccount().getName());
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + vaultAccountName).getBytes(StandardCharsets.UTF_8));
    }

    private String processAccount(String accountName) {
        String providerName = QuickShop.getInstance().getEconomy().getProviderName();
        if (JavaPlugin.getPlugin(Main.class).getConfig().getBoolean("workaround-for-account-name") || "Essentials".equals(providerName)) {
            return EssStringUtil.safeString(accountName);
        }
        return accountName;
    }
}
