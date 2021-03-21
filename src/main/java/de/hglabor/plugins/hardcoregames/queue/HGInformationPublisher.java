package de.hglabor.plugins.hardcoregames.queue;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.utils.noriskutils.jedis.JChannels;
import de.hglabor.utils.noriskutils.jedis.JedisUtils;
import de.hglabor.utils.noriskutils.queue.hg.HGGameInfo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class HGInformationPublisher extends BukkitRunnable {
    private final String serverName;

    public HGInformationPublisher(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void run() {
        HGGameInfo hgInfo = new HGGameInfo(
                Bukkit.getMaxPlayers(),
                GameStateManager.INSTANCE.getPhase().getCurrentParticipants(),
                GameStateManager.INSTANCE.getPhase().getRawTime(),
                GameStateManager.INSTANCE.getPhase().getType().name(),
                serverName);
        JedisUtils.publish(JChannels.HGQUEUE_INFO, HardcoreGames.GSON.toJson(hgInfo, HGGameInfo.class));
    }
}
