package de.hglabor.plugins.hardcoregames;

import com.google.gson.Gson;
import de.hglabor.plugins.hardcoregames.command.*;
import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.mechanics.MooshroomCowNerf;
import de.hglabor.plugins.hardcoregames.game.unknown.PlayerChangeWorldListener;
import de.hglabor.plugins.hardcoregames.kit.KitSelectorImpl;
import de.hglabor.plugins.hardcoregames.player.HGPlayer;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import de.hglabor.plugins.hardcoregames.queue.QueueChannel;
import de.hglabor.plugins.hardcoregames.scoreboard.ScoreboardJoinListener;
import de.hglabor.plugins.hardcoregames.scoreboard.ScoreboardManager;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.command.KitSettingsCommand;
import de.hglabor.plugins.kitapi.kit.events.KitEventHandlerImpl;
import de.hglabor.plugins.kitapi.kit.items.KitItemHandler;
import de.hglabor.plugins.kitapi.listener.LastHitDetection;
import de.hglabor.plugins.kitapi.pvp.CPSChecker;
import de.hglabor.plugins.kitapi.pvp.SoupHealing;
import de.hglabor.plugins.kitapi.pvp.Tracker;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.DataPackUtils;
import de.hglabor.utils.noriskutils.command.HidePlayersCommand;
import de.hglabor.utils.noriskutils.listener.DamageNerf;
import de.hglabor.utils.noriskutils.listener.DurabilityFix;
import de.hglabor.utils.noriskutils.listener.OldKnockback;
import de.hglabor.utils.noriskutils.listener.RemoveHitCooldown;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardFactory;
import de.hglabor.utils.noriskutils.staffmode.PlayerHider;
import de.hglabor.utils.noriskutils.staffmode.StaffModeCommand;
import de.hglabor.utils.noriskutils.staffmode.StaffModeListener;
import de.hglabor.utils.noriskutils.staffmode.StaffModeManager;
import de.hglabor.velocity.queue.constants.QChannels;
import de.hglabor.velocity.queue.jedis.JedisManager;
import de.hglabor.velocity.queue.pojo.QGameInfo;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class HardcoreGames extends JavaPlugin {
    public static final Gson GSON = new Gson();
    public static HardcoreGames plugin;
    public static String serverName;
    private static String SHARED_SERVER_DATA;

    public static HardcoreGames getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        DataPackUtils.generateNewWorld(HGConfig.getString(ConfigKeys.SERVER_FOLDER_PATH) + serverName, "hg");

        StaffModeManager.INSTANCE.setPlayerHider(new PlayerHider(PlayerList.INSTANCE, this));
        KitApi.getInstance().register(PlayerList.INSTANCE, new KitSelectorImpl(), this, Paths.get("/home/", getConfig().getString(ConfigKeys.SERVER_KIT_CONFIG_PATH)));
        CommandAPI.onEnable(this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, QChannels.QUEUE_JOIN.get());
        this.registerEvents();

        GameStateManager.INSTANCE.init();
        GameStateManager.INSTANCE.run();

        for (Player player : Bukkit.getOnlinePlayers()) {
            HGPlayer hgPlayer = PlayerList.INSTANCE.getPlayer(player);
            ScoreboardFactory.create(hgPlayer);
            ScoreboardFactory.addPlayerToNoCollision(player, hgPlayer);
            ScoreboardManager.setBasicScoreboardLayout(hgPlayer);
        }

        registerCommands();
        initJedis();
    }

    @Override
    public void onLoad() {
        plugin = this;
        HGConfig.load();
        SHARED_SERVER_DATA = HGConfig.getString(ConfigKeys.SERVER_SHARED_SERVER_DATA);
        serverName = HGConfig.getString(ConfigKeys.SERVER_NAME);
        loadLocalizationFiles();
        CommandAPI.onLoad(true);
    }

    @Override
    public void onDisable() {
        if (HGConfig.getBoolean(ConfigKeys.SERVER_PING)) {
            JedisManager.publish(QChannels.QUEUE_INFO.get(), GSON.toJson(new QGameInfo(serverName, "RESTARTING")));
            JedisManager.closePool();
        }
    }

    private void initJedis() {
        if (HGConfig.getBoolean(ConfigKeys.SERVER_PING)) {
            JedisManager.init(HGConfig.getString(ConfigKeys.REDIS_PW));
            JedisManager.subscribe(new QueueChannel(serverName), QChannels.QUEUE_JOIN.get(), QChannels.QUEUE_LEAVE.get());
        }
    }

    private void registerCommands() {
        new KitCommand();
        new StaffModeCommand(PlayerList.INSTANCE);
        new HidePlayersCommand();
        new StartCommand();
        new ListCommand();
        new FeastCommand();
        new ExtendCommand();
        new KitSettingsCommand(true);
    }

    private void registerEvents() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ScoreboardJoinListener(), this);
        pluginManager.registerEvents(new RemoveHitCooldown(), this);
        pluginManager.registerEvents(new OldKnockback(this), this);
        pluginManager.registerEvents(new DurabilityFix(), this);
        pluginManager.registerEvents(new DamageNerf(HGConfig.getDouble(ConfigKeys.SWORD_DAMAGE_NERF), HGConfig.getDouble(ConfigKeys.OTHER_TOOLS_DAMAGE_NERF)), this);
        pluginManager.registerEvents(new LastHitDetection(), this);
        pluginManager.registerEvents(new StaffModeListener(PlayerList.INSTANCE), this);
        pluginManager.registerEvents(new KitItemHandler(), this);
        pluginManager.registerEvents(new KitEventHandlerImpl(), this);
        pluginManager.registerEvents(new CPSChecker(), this);
        pluginManager.registerEvents(new Tracker(HGConfig.getDouble(ConfigKeys.TRACKER_DISTANCE), PlayerList.INSTANCE), this);
        pluginManager.registerEvents(new SoupHealing(), this);
        pluginManager.registerEvents(new MooshroomCowNerf(), this);
        pluginManager.registerEvents(new PlayerChangeWorldListener(), this);
    }

    private void loadLocalizationFiles() {
        try {
            //U+00A7 = §
            Localization.INSTANCE.loadLanguageFiles(Paths.get(SHARED_SERVER_DATA, "localization", "HG"), "\u00A7");
            Localization.INSTANCE.loadLanguageFiles(Paths.get(SHARED_SERVER_DATA, "localization", "KitApi"), "\u00A7");
            Localization.INSTANCE.loadLanguageFiles(Paths.get(SHARED_SERVER_DATA, "localization", "Staff"), "\u00A7");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
