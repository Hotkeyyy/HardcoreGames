package de.hglabor.plugins.hardcoregames.game.phase;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.google.common.collect.ImmutableMap;
import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.plugins.hardcoregames.game.GamePhase;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.PhaseType;
import de.hglabor.plugins.hardcoregames.player.HGPlayer;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import de.hglabor.plugins.hardcoregames.player.PlayerStatus;
import de.hglabor.plugins.hardcoregames.queue.GameInfoProvider;
import de.hglabor.plugins.hardcoregames.util.Logger;
import de.hglabor.plugins.hardcoregames.util.RandomTeleport;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.ItemBuilder;
import de.hglabor.utils.noriskutils.PotionUtils;
import de.hglabor.utils.noriskutils.TimeConverter;
import de.hglabor.velocity.queue.constants.QChannels;
import de.hglabor.velocity.queue.jedis.JedisManager;
import de.hglabor.velocity.queue.pojo.QGameInfo;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class LobbyPhase extends GamePhase {
    protected final ItemStack QUEUE_ITEM, RANDOM_TP;
    private final String serverName;
    protected int forceStartTime, prepareStartTime, timeLeft, requiredPlayerAmount;
    protected boolean isStarting, isForceStarting;

    public LobbyPhase() {
        super(HGConfig.getInteger(ConfigKeys.LOBBY_WAITING_TIME));
        this.forceStartTime = HGConfig.getInteger(ConfigKeys.COMMAND_FORCESTART_TIME);
        this.requiredPlayerAmount = HGConfig.getInteger(ConfigKeys.LOBBY_PLAYERS_NEEDED);
        this.prepareStartTime = HGConfig.getInteger(ConfigKeys.LOBBY_PREPARE_START_TIME);
        this.serverName = HGConfig.getString(ConfigKeys.SERVER_NAME);
        //TODO add desc and maybe localization
        this.QUEUE_ITEM = new ItemBuilder(Material.HEART_OF_THE_SEA).setName(ChatColor.GREEN + "Queue").build();
        this.RANDOM_TP = new ItemBuilder(Material.LODESTONE).setName(ChatColor.AQUA + "Random Teleport").build();
    }

    @Override
    protected void init() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Optional<World> world = Optional.ofNullable(Bukkit.getWorld("world"));
        world.ifPresent(HGConfig::lobbyWorldSettings);
        GameInfoProvider hgInformationPublisher = new GameInfoProvider(serverName);
        hgInformationPublisher.runTaskTimerAsynchronously(HardcoreGames.getPlugin(), 0, 20);
    }

    @Override
    protected void tick(int timer) {
        if (playerList.getWaitingPlayers().size() >= requiredPlayerAmount) {
            timeLeft = maxPhaseTime - timer;
            announceRemainingTime(timeLeft);

            if (timeLeft == prepareStartTime && !isForceStarting) {
                prepareToStart();
            }

            if (isStarting()) {
                JedisManager.publish(QChannels.QUEUE_MOVE.get(), serverName);
            }

            if (timeLeft <= 0) {
                GameStateManager.INSTANCE.resetTimer();
                if (PlayerList.INSTANCE.getWaitingPlayers().size() >= requiredPlayerAmount) {
                    //TODO SOUNDS
                    startNextPhase();
                    ChatUtils.broadcastMessage("lobbyPhase.gameStarts");
                } else {
                    ChatUtils.broadcastMessage("lobbyPhase.notEnoughPlayers", ImmutableMap.of("requiredPlayers", String.valueOf(requiredPlayerAmount)));
                    prepareToWait();
                }
            }
        } else {
            prepareToWait();
            GameStateManager.INSTANCE.resetTimer();
        }
    }

    public void prepareToStart() {
        isStarting = true;
        for (HGPlayer waitingPlayer : playerList.getWaitingPlayers()) {
            waitingPlayer.getBukkitPlayer().ifPresent(player -> {
                PotionUtils.paralysePlayer(player);
                player.getInventory().removeItem(QUEUE_ITEM);
                player.getInventory().removeItem(RANDOM_TP);
            });
            waitingPlayer.teleportToSafeSpawn();
        }
    }

    public void prepareToWait() {
        if (isStarting) {
            Logger.debug("Setting all players lobby rdy");
            isStarting = false;
            isForceStarting = false;
            requiredPlayerAmount = HGConfig.getInteger(ConfigKeys.LOBBY_PLAYERS_NEEDED);
            playerList.getWaitingPlayers().forEach(player -> player.getBukkitPlayer().ifPresent(this::setPlayerLobbyReady));
        }
    }

    private void announceRemainingTime(int timeLeft) {
        if (timeLeft % 30 == 0 || timeLeft <= 5 || timeLeft == 15 || timeLeft == 10) {
            ChatUtils.broadcastMessage("lobbyPhase.timeAnnouncement", ImmutableMap.of("timeString", TimeConverter.stringify(timeLeft)));
        }
    }

    @Override
    public PhaseType getType() {
        return PhaseType.LOBBY;
    }

    @Override
    public int getRawTime() {
        int rawTime = super.getRawTime();
        return rawTime == 0 ? maxPhaseTime - rawTime : timeLeft;
    }

    public boolean isStarting() {
        return isStarting;
    }

    public void setForceStarted(boolean forceStarted) {
        isForceStarting = forceStarted;
    }

    @Override
    public String getTimeString(int timer) {
        return TimeConverter.stringify(getRawTime());
    }

    @Override
    public int getCurrentParticipants() {
        return playerList.getWaitingPlayers().size();
    }

    @Override
    public int getMaxParticipants() {
        return getCurrentParticipants();
    }

    @Override
    protected GamePhase getNextPhase() {
        return new InvincibilityPhase();
    }

    public void setPlayerLobbyReady(Player player) {
        player.getInventory().clear();
        player.setHealth(20);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setAllowFlight(false);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        PotionUtils.removePotionEffects(player);
        KitApi.getInstance().getKitSelector().getKitSelectorItems().forEach(item -> player.getInventory().addItem(item));
        if (isStarting) {
            PotionUtils.paralysePlayer(player);
        } else {
            player.getInventory().addItem(QUEUE_ITEM);
            player.getInventory().addItem(RANDOM_TP);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HGPlayer hgPlayer = playerList.getPlayer(player);
        hgPlayer.setStatus(PlayerStatus.WAITING);
        hgPlayer.teleportToSafeSpawn();
        setPlayerLobbyReady(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        HGPlayer hgPlayer = playerList.getPlayer(event.getPlayer());
        if (!hgPlayer.getStatus().equals(PlayerStatus.QUEUE)) {
            playerList.remove(hgPlayer);
        }
    }

    @EventHandler
    public void onRightClickQueueItem(PlayerInteractEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.isSimilar(QUEUE_ITEM)) {
            HGPlayer hgPlayer = playerList.getPlayer(player);
            QGameInfo gameInfo = new QGameInfo(serverName, true);
            hgPlayer.setStatus(PlayerStatus.QUEUE);
            player.sendPluginMessage(HardcoreGames.getPlugin(), QChannels.QUEUE_JOIN.get(), HardcoreGames.GSON.toJson(gameInfo, QGameInfo.class).getBytes());
        } else if (item.isSimilar(RANDOM_TP)) {
            RandomTeleport.teleportAsync(player);
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        if (isStarting) event.setCancelled(true);
    }

    @EventHandler
    private void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onEntityDamageEvent(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockPlaceEvent(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockBreakEvent(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerPickupExperienceEvent(PlayerPickupExperienceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerAttemptPickupItemEvent(PlayerAttemptPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onInventoryClickEvent(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        event.setCancelled(true);
    }

    public int getRequiredPlayerAmount() {
        return requiredPlayerAmount;
    }

    public void setRequiredPlayerAmount(int requiredPlayerAmount) {
        this.requiredPlayerAmount = requiredPlayerAmount;
    }
}
