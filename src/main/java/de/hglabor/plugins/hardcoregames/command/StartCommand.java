package de.hglabor.plugins.hardcoregames.command;

import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.PhaseType;
import de.hglabor.plugins.hardcoregames.game.phase.LobbyPhase;
import de.hglabor.plugins.hardcoregames.util.Logger;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.PermissionUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.ChatColor;

import static de.hglabor.utils.localization.Localization.t;

public class StartCommand {

    public StartCommand() {
        new CommandAPICommand("start")
                .withAliases("fs","forcestart","begin")
                .withPermission("hglabor.forcestart")
                .withRequirement((commandSender) -> GameStateManager.INSTANCE.getPhase().getType().equals(PhaseType.LOBBY))
                .executesPlayer((player, objects) -> {
                    if (PermissionUtils.checkForHigherRank(player)) {
                        player.sendMessage(t("hglabor.higherRankIsOnline", ChatUtils.getPlayerLocale(player)));
                        return;
                    }
                    LobbyPhase lobbyPhase = (LobbyPhase) GameStateManager.INSTANCE.getPhase();
                    lobbyPhase.prepareToStart();
                    lobbyPhase.setForceStarted(true);
                    lobbyPhase.setRequiredPlayerAmount(1);
                    GameStateManager.INSTANCE.setTimer((lobbyPhase.getMaxPhaseTime() - HGConfig.getInteger(ConfigKeys.COMMAND_FORCESTART_TIME)));
                    player.sendMessage(Localization.INSTANCE.getMessage("permissions.roundHasBeenStarted", ChatUtils.getPlayerLocale(player)));
                    Logger.debug(String.format("%s has forcestarted", player.getName()));
                })
                .register();
    }
}
