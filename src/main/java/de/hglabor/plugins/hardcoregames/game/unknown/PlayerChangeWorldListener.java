package de.hglabor.plugins.hardcoregames.game.unknown;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import de.hglabor.plugins.hardcoregames.player.HGPlayer;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import de.hglabor.velocity.queue.pojo.QPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        HGPlayer hgPlayer = PlayerList.INSTANCE.getPlayer(player);
        if (player.getWorld().equals(Bukkit.getWorld("schematic"))) {
            hgPlayer.teleportToSafeSpawn();
        }
    }

    @EventHandler
    public void onEntityAddToWorld(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getWorld().equals(Bukkit.getWorld("schematic"))) {
            HGPlayer hgPlayer = PlayerList.INSTANCE.getPlayer((QPlayerInfo) event.getEntity());
            hgPlayer.teleportToSafeSpawn();
        }
    }
}
