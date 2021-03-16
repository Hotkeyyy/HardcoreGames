package de.hglabor.plugins.hardcoregames.scoreboard;

import de.hglabor.plugins.hardcoregames.game.GamePhase;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.PhaseType;
import de.hglabor.plugins.hardcoregames.player.HGPlayer;
import de.hglabor.plugins.hardcoregames.player.PlayerList;
import de.hglabor.plugins.kitapi.config.KitApiConfig;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.kits.CopyCatKit;
import de.hglabor.plugins.kitapi.kit.kits.NoneKit;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardFactory;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardPlayer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class ScoreboardManager {
    private final static String SPACE = " ";

    private ScoreboardManager() {
    }

    public static void setBasicScoreboardLayout(ScoreboardPlayer scoreboardPlayer) {
        int kitAmount = KitApiConfig.getInstance().getInteger("kit.amount");
        int lowestPosition = 7;
        int highestPosition = lowestPosition + kitAmount;
        ScoreboardFactory.addEntry(scoreboardPlayer, "gameState", Localization.INSTANCE.getMessage(
                "scoreboard.gameState." + GameStateManager.INSTANCE.getPhase().getType().name().toLowerCase(),
                scoreboardPlayer.getLocale()), "",
                highestPosition + 3);
        ScoreboardFactory.addEntry(scoreboardPlayer, "gameStateTime", "00:00", "", highestPosition + 2);
        ScoreboardFactory.addEntry(scoreboardPlayer, String.valueOf(highestPosition + 1), "", "", highestPosition + 1);
        if (kitAmount == 1) {
            ScoreboardFactory.addEntry(scoreboardPlayer, "kitValue" + 1, "Kit: None", "", highestPosition);
        } else if (kitAmount > 1) {
            for (int i = highestPosition; i > lowestPosition; i--) {
                ScoreboardFactory.addEntry(scoreboardPlayer, "kitValue" + (i - lowestPosition), "Kit" + (i - lowestPosition) + ": None", "", i);
            }
        }
        ScoreboardFactory.addEntry(scoreboardPlayer, "killsValue", "Kills: 0", "", lowestPosition);
        ScoreboardFactory.addEntry(scoreboardPlayer, "6", "", "", 6);
        ScoreboardFactory.addEntry(scoreboardPlayer, "players", Localization.INSTANCE.getMessage("scoreboard.players", scoreboardPlayer.getLocale()), "", 5);
        int alivePlayerSize = PlayerList.INSTANCE.getAlivePlayers().size();
        ScoreboardFactory.addEntry(scoreboardPlayer, "playersValue", alivePlayerSize + "/" + alivePlayerSize, "", 4);
        ScoreboardFactory.addEntry(scoreboardPlayer, "3", "", "", 3);
    }

    public static void updateForEveryone(String timeString) {
        for (HGPlayer hgPlayer : PlayerList.INSTANCE.getPlayers()) {
            hgPlayer.getBukkitPlayer().ifPresent(player -> {
                ScoreboardFactory.updateEntry(hgPlayer, "gameState", createGameStateMessage(hgPlayer));
                createPlayersEntry(hgPlayer);
                ScoreboardFactory.updateEntry(hgPlayer, "killsValue", createKillsMessage(hgPlayer));
                ScoreboardFactory.updateEntry(hgPlayer, "gameStateTime", createTimeStringMessage(timeString));
                createKitEntries(hgPlayer);
            });
        }
    }

    private static void createPlayersEntry(HGPlayer hgPlayer) {
        GamePhase phase = GameStateManager.INSTANCE.getPhase();
        if (phase.getType().equals(PhaseType.LOBBY)) {
            int queuePlayers = PlayerList.INSTANCE.getQueueingPlayers().size();
            String inQueue = queuePlayers > 0 ? String.format(" (%d in queue) ", queuePlayers) : "";
            ScoreboardFactory.updateEntry(hgPlayer, "playersValue", SPACE + phase.getCurrentParticipants() + inQueue + "/" + phase.getMaxParticipants());
        } else {
            ScoreboardFactory.updateEntry(hgPlayer, "playersValue", SPACE + phase.getCurrentParticipants() + "/" + phase.getMaxParticipants());
        }
    }

    private static void createKitEntries(HGPlayer hgPlayer) {
        boolean kitDisabled = hgPlayer.areKitsDisabled();
        if (KitApiConfig.getInstance().getKitAmount() > 0) {
            int index = 1;
            for (AbstractKit kit : hgPlayer.getKits()) {
                if (kit.equals(CopyCatKit.INSTANCE)) {
                    AbstractKit copiedKit = hgPlayer.getKitAttribute(CopyCatKit.INSTANCE.getKitAttributeKey());
                    ScoreboardFactory.updateEntry(hgPlayer, "kitValue" + index, createCopiedKitMessage(kitDisabled, index, kit, copiedKit));
                } else {
                    ScoreboardFactory.updateEntry(hgPlayer, "kitValue" + index, createDefaultKitMessage(kitDisabled, index, kit));
                }
                index++;
            }
        }
    }

    @NotNull
    private static String createDefaultKitMessage(boolean kitDisabled, int index, AbstractKit kit) {
        String kitIndex = KitApiConfig.getInstance().getKitAmount() == 1 ? "" : String.valueOf(index);
        if (kitDisabled) {
            return String.format("%s%sKit%s: %s%s%s", ChatColor.BLUE, ChatColor.BOLD, kitIndex, ChatColor.RESET, ChatColor.STRIKETHROUGH, kit.getName());
        }
        return String.format("%s%sKit%s: %s%s", ChatColor.BLUE, ChatColor.BOLD, kitIndex, ChatColor.RESET, kit.getName());
    }

    @NotNull
    private static String createCopiedKitMessage(boolean kitDisabled, int index, AbstractKit kit, AbstractKit copiedKit) {
        String copiedKitName = copiedKit != null ? copiedKit.getName() : NoneKit.INSTANCE.getName();
        String kitIndex = index == 1 ? "" : String.valueOf(index);
        if (kitDisabled) {
            return String.format("%s%sKit%s: %s%s%s(%s)", ChatColor.BLUE, ChatColor.BOLD, kitIndex, ChatColor.RESET, ChatColor.STRIKETHROUGH, kit.getName(), copiedKitName);
        }
        return String.format("%s%sKit%s: %s%s(%s)", ChatColor.BLUE, ChatColor.BOLD, kitIndex, ChatColor.RESET, kit.getName(), copiedKitName);
    }

    @NotNull
    private static String createTimeStringMessage(String timeString) {
        return SPACE + timeString;
    }

    private static String createGameStateMessage(HGPlayer hgPlayer) {
        return Localization.INSTANCE.getMessage("scoreboard.gameState." + GameStateManager.INSTANCE.getPhase().getType().name().toLowerCase(), hgPlayer.getLocale());
    }

    @NotNull
    private static String createKillsMessage(HGPlayer hgPlayer) {
        return ChatColor.AQUA + "" + ChatColor.BOLD + "Kills: " + ChatColor.RESET + hgPlayer.getKills().get();
    }
}
