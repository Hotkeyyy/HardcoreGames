package de.hglabor.plugins.hardcoregames.queue;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.PhaseType;
import de.hglabor.plugins.hardcoregames.player.HGPlayer;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import de.hglabor.plugins.hardcoregames.player.PlayerStatus;
import de.hglabor.plugins.hardcoregames.util.Logger;
import de.hglabor.velocity.queue.constants.QChannels;
import de.hglabor.velocity.queue.pojo.QPlayerInfo;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class QueueChannel extends JedisPubSub {
    private final String serverName;

    public QueueChannel(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void onMessage(String channel, String message) {
        Logger.debug(String.format("Redis channel: %s with message %s", channel, message));
        boolean isLobby = GameStateManager.INSTANCE.getPhase().getType().equals(PhaseType.LOBBY);
        if (channel.equalsIgnoreCase(QChannels.QUEUE_JOIN.get())) {
            QPlayerInfo qPlayerInfo = HardcoreGames.GSON.fromJson(message, QPlayerInfo.class);
            if (qPlayerInfo.getServerName().equalsIgnoreCase(serverName) && isLobby) {
                HGPlayer hgPlayer = PlayerList.INSTANCE.getPlayer(qPlayerInfo);
                hgPlayer.setStatus(PlayerStatus.QUEUE);
                Logger.debug(String.format("Added to Queue via redis: %s with server %s", hgPlayer.getName(), qPlayerInfo.getServerName()));
            }
        } else if (channel.equalsIgnoreCase(QChannels.QUEUE_LEAVE.get())) {
            HGPlayer hgPlayer = PlayerList.INSTANCE.getPlayer(UUID.fromString(message));
            if (hgPlayer != null && hgPlayer.getStatus().equals(PlayerStatus.QUEUE) && isLobby) {
                hgPlayer.getBukkitPlayer().ifPresentOrElse(
                        player -> hgPlayer.setStatus(PlayerStatus.WAITING),
                        () -> PlayerList.INSTANCE.remove(UUID.fromString(message)));
            }
        }
    }
}
