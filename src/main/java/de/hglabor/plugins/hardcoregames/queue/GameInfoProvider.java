package de.hglabor.plugins.hardcoregames.queue;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.phase.LobbyPhase;
import de.hglabor.velocity.queue.constants.QChannels;
import de.hglabor.velocity.queue.jedis.JedisManager;
import de.hglabor.velocity.queue.pojo.QGameInfo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class GameInfoProvider extends BukkitRunnable {
    private final String serverName;

    public GameInfoProvider(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void run() {
        GameStateManager manager = GameStateManager.INSTANCE;
        QGameInfo gameInfo = new QGameInfo(
                serverName,
                manager.getPhase().getType().name(),
                manager.getPhase() instanceof LobbyPhase ? ((LobbyPhase) manager.getPhase()).getRequiredPlayerAmount() : 0,
                Bukkit.getMaxPlayers(),
                manager.getPhase().getCurrentParticipants(),
                manager.getPhase().getRawTime(),
                true
        );
        JedisManager.publish(QChannels.QUEUE_INFO.get(), HardcoreGames.GSON.toJson(gameInfo, QGameInfo.class));
    }
}
