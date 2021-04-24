package de.hglabor.plugins.hardcoregames.queue;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.phase.LobbyPhase;
import de.hglabor.velocity.queue.constants.QChannels;
import de.hglabor.velocity.queue.jedis.JedisManager;
import de.hglabor.velocity.queue.pojo.QGameInfo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class GameInfoProvider extends BukkitRunnable {
    private final String serverName;

    public GameInfoProvider(String serverName) {
        this.serverName = serverName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        GameStateManager manager = GameStateManager.INSTANCE;
        QGameInfo gameInfo = new QGameInfo(
                serverName,
                manager.getPhase().getType().name(),
                manager.getPhase() instanceof LobbyPhase ? ((LobbyPhase) manager.getPhase()).getRequiredPlayerAmount() : 0,
                Bukkit.getMaxPlayers(),
                manager.getPhase().getCurrentParticipants(),
                manager.getPhase().getTimeString(manager.getTimer()),
                true
        );
        List<String> list = (List<String>) HGConfig.getList(ConfigKeys.QUEUE_INFO);
        gameInfo.setAdditionalInfo(list);
        JedisManager.publish(QChannels.QUEUE_INFO.get(), HardcoreGames.GSON.toJson(gameInfo, QGameInfo.class));
    }
}
