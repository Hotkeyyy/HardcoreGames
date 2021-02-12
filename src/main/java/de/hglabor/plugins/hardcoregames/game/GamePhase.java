package de.hglabor.plugins.hardcoregames.game;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GamePhase implements Listener {
    protected final JavaPlugin plugin;
    protected final PlayerList playerList;
    protected final int maxPhaseTime;

    protected GamePhase(int maxPhaseTime) {
        this.maxPhaseTime = maxPhaseTime;
        this.plugin = HardcoreGames.getPlugin();
        this.playerList = PlayerList.INSTANCE;
    }

    public void init() {
    }

    public abstract void tick(int timer);

    public abstract PhaseType getType();

    public abstract String getTimeString(int timer);

    protected void startNextPhase() {
        HandlerList.unregisterAll(this);
        GamePhase nextPhase = getNextPhase();
        nextPhase.init();
        Bukkit.getPluginManager().registerEvents(nextPhase, plugin);
        GameStateManager.INSTANCE.setPhase(nextPhase);
    }

    protected abstract GamePhase getNextPhase();
}