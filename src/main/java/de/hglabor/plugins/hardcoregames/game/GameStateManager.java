package de.hglabor.plugins.hardcoregames.game;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.plugins.hardcoregames.game.phase.LobbyPhase;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import de.hglabor.plugins.hardcoregames.scoreboard.ScoreboardManager;
import de.hglabor.plugins.kitapi.pvp.SkyBorder;
import de.hglabor.utils.noriskutils.staffmode.StaffModeManager;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;

public final class GameStateManager {
    public static final GameStateManager INSTANCE = new GameStateManager();
    private final AtomicInteger timer;
    private GamePhase phase;
    private SkyBorder skyBorder;

    private GameStateManager() {
        this.timer = new AtomicInteger();
        this.phase = new LobbyPhase();
    }

    public void init() {
        this.skyBorder = new SkyBorder(
                HardcoreGames.getPlugin(),
                HGConfig.getInteger(ConfigKeys.SKY_BORDER_HEIGHT),
                HGConfig.getInteger(ConfigKeys.SKY_BORDER_DAMAGE));
    }

    public void run() {
        phase.init();
        Bukkit.getScheduler().runTaskTimer(HardcoreGames.getPlugin(), () -> {
            final int CURRENT_TIME = timer.getAndIncrement();
            phase.tick(CURRENT_TIME);
            ScoreboardManager.updateForEveryone(phase.getTimeString(CURRENT_TIME));
            skyBorder.tick(PlayerList.INSTANCE.getOnlineEntityPlayers());
            StaffModeManager.INSTANCE.getPlayerHider().sendHideInformation();
        }, 0, 20L);
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public void resetTimer() {
        timer.set(0);
    }

    public int getTimer() {
        return timer.get();
    }

    public void setTimer(int value) {
        timer.set(value);
    }
}
