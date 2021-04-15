package de.hglabor.plugins.hardcoregames.config;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;

public class HGConfig {
    private HGConfig() {
    }

    public static void load() {
        HardcoreGames plugin = HardcoreGames.getPlugin();
        plugin.getConfig().addDefault(ConfigKeys.LOBBY_PLAYERS_NEEDED, 2);
        plugin.getConfig().addDefault(ConfigKeys.LOBBY_WAITING_TIME, 180);
        plugin.getConfig().addDefault(ConfigKeys.LOBBY_PREPARE_START_TIME, 15);
        plugin.getConfig().addDefault(ConfigKeys.INVINCIBILITY_TIME, 120);
        plugin.getConfig().addDefault(ConfigKeys.INGAME_MAX_PLAYTIME, 1800);
        plugin.getConfig().addDefault(ConfigKeys.END_RESTART_AFTER, 10);

        plugin.getConfig().addDefault(ConfigKeys.PLAYER_OFFLINE_TIME, 90);
        plugin.getConfig().addDefault(ConfigKeys.COMMAND_FORCESTART_TIME, 20);
        plugin.getConfig().addDefault(ConfigKeys.FEAST_EARLIEST_APPEARANCE, 600);
        plugin.getConfig().addDefault(ConfigKeys.FEAST_LATEST_APPEARANCE, 900);
        plugin.getConfig().addDefault(ConfigKeys.FEAST_TIME_TILL_SPAWN, 300);
        plugin.getConfig().addDefault(ConfigKeys.SKY_BORDER_DAMAGE, 6);
        plugin.getConfig().addDefault(ConfigKeys.DEBUG_IS_ENABLED, false);
        plugin.getConfig().addDefault(ConfigKeys.REDIS_PW, "password");
        plugin.getConfig().addDefault(ConfigKeys.SERVER_PING, true);
        plugin.getConfig().addDefault(ConfigKeys.SERVER_NAME, "hg");
        plugin.getConfig().addDefault(ConfigKeys.SERVER_EVENT, false);
        plugin.getConfig().addDefault(ConfigKeys.SWORD_DAMAGE_NERF, 0.65);
        plugin.getConfig().addDefault(ConfigKeys.OTHER_TOOLS_DAMAGE_NERF, 0.2);
        plugin.getConfig().addDefault(ConfigKeys.TRACKER_DISTANCE, 30D);
        plugin.getConfig().addDefault(ConfigKeys.MOOSHROOM_COW_NERF_MAX_SOUPS_FROM_COW, 27);
        plugin.getConfig().addDefault(ConfigKeys.MOOSHROOM_COW_NERF_COMBAT_MULTIPLIER, 2);
        plugin.getConfig().addDefault(ConfigKeys.MAX_RECRAFT_AMOUNT, 64);
        plugin.getConfig().addDefault(ConfigKeys.WORLD_BORDER_SIZE, 500);
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public static void lobbyWorldSettings(World world) {
        world.setSpawnLocation(new Location(world, 0, world.getHighestBlockYAt(0, 0), 0));
        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);
        world.getWorldBorder().setSize(getInteger(ConfigKeys.WORLD_BORDER_SIZE) * 2);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        //world.setGameRule(GameRule.DO_LIMITED_CRAFTING,true);
    }

    public static void inGameWorldSettings(World world) {
        world.setTime(0);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, true);
        world.setGameRule(GameRule.MOB_GRIEFING, true);
    }

    public static int getInteger(String key) {
        return HardcoreGames.getPlugin().getConfig().getInt(key);
    }

    public static String getString(String key) {
        return HardcoreGames.getPlugin().getConfig().getString(key);
    }

    public static double getDouble(String key) {
        return HardcoreGames.getPlugin().getConfig().getDouble(key);
    }

    public static boolean getBoolean(String key) {
        return HardcoreGames.getPlugin().getConfig().getBoolean(key);
    }

    public static boolean isEvent() {
        return HardcoreGames.getPlugin().getConfig().getBoolean(ConfigKeys.SERVER_EVENT);
    }
}
