package de.hglabor.plugins.hardcoregames.config;

public interface ConfigKeys {
    String LOBBY = "lobby";
    String LOBBY_PLAYERS_NEEDED = LOBBY + "." + "playersToStart";
    String LOBBY_WAITING_TIME = LOBBY + "." + "timeToWait";
    String LOBBY_PREPARE_START_TIME = LOBBY + "." + "prepareStartTime";

    String INVINCIBILITY = "invincibility";
    String INVINCIBILITY_TIME = INVINCIBILITY + "." + "time";

    String INGAME = "ingame";
    String INGAME_MAX_PLAYTIME = INGAME + "." + "maxPlayTime";

    String END = "end";
    String END_RESTART_AFTER = END + "." + "restartAfter";

    String PLAYER = "player";
    String PLAYER_OFFLINE_TIME = PLAYER + "." + "offlineTime";

    String COMMAND = "command";
    String COMMAND_FORCESTART = COMMAND + "." + "forcestart";
    String COMMAND_FORCESTART_TIME = COMMAND_FORCESTART + "." + "time";

    String FEAST = "feast";
    String FEAST_LATEST_APPEARANCE = FEAST + "." + "latestAppearance";
    String FEAST_TIME_TILL_SPAWN = FEAST + "." + "timeTillSpawn";
    String FEAST_EARLIEST_APPEARANCE = FEAST + "." + "earliestAppearance";

    String BORDER = "border";
    String SKY_BORDER = "skyborder";
    String SKY_BORDER_DAMAGE = BORDER + "." + SKY_BORDER + "." + "damage";

    String DEBUG = "debug";
    String DEBUG_IS_ENABLED = DEBUG + "." + "enabled";

    String REDIS = "redis";
    String REDIS_PW = REDIS + "." + "password";

    String SERVER = "server";
    String SERVER_PING = SERVER + "." + "ping";

    String MECHANICS = "mechanics";
    String SWORD_DAMAGE_NERF = MECHANICS + "." + "sworddamagemultiplier";
    String MAX_RECRAFT_AMOUNT = MECHANICS + "." + "maxRecraftAmount";
    String OTHER_TOOLS_DAMAGE_NERF = MECHANICS + "." + "othertoolsdamagemultiplier";
    String TRACKER_DISTANCE = MECHANICS + "." + "trackerDistance";
    String WORLD_BORDER_SIZE = MECHANICS + "." + "worldBorderSize";
    String MOOSHROOM_COW_NERF_MAX_SOUPS_FROM_COW = MECHANICS + "." + "maxsoupsfromcow";
    String MOOSHROOM_COW_NERF_COMBAT_MULTIPLIER = MECHANICS + "." + "soupsinaddition"; // For players in Combat
}
