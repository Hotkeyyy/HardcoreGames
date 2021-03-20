package de.hglabor.plugins.hardcoregames.util;

import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.TeleportUtils;
import org.bukkit.HeightMap;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.hglabor.utils.localization.Localization.t;

public final class RandomTeleport {
    private static final Map<UUID, Long> cooldown = new HashMap<>();

    private RandomTeleport() {
    }

    public static void teleportAsync(Player player) {
        if (cooldown.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) > System.currentTimeMillis()) {
            player.sendMessage(t("randomteleport.spamProt", ChatUtils.getPlayerLocale(player)));
        } else {
            int borderSize = HGConfig.getInteger(ConfigKeys.WORLD_BORDER_SIZE);
            player.teleportAsync(TeleportUtils.getHighestRandomLocation(player.getWorld(), borderSize, -borderSize, HeightMap.MOTION_BLOCKING_NO_LEAVES));
            cooldown.put(player.getUniqueId(), System.currentTimeMillis() + 1500L);
        }
    }
}
