package de.hglabor.plugins.hardcoregames.command;

import de.hglabor.plugins.hardcoregames.config.ConfigKeys;
import de.hglabor.plugins.hardcoregames.config.HGConfig;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.PhaseType;
import de.hglabor.plugins.hardcoregames.game.phase.LobbyPhase;
import de.hglabor.plugins.hardcoregames.util.Logger;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.PermissionUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;

import static de.hglabor.utils.localization.Localization.t;

public class ExtendCommand {

    public ExtendCommand() {
        new CommandAPICommand("extend")
                .withPermission("hglabor.forcestart")
                .withRequirement((commandSender) -> GameStateManager.INSTANCE.getPhase().getType().equals(PhaseType.LOBBY))
                .withArguments(new IntegerArgument("seconds", HGConfig.getInteger(ConfigKeys.LOBBY_PREPARE_START_TIME) + 1, 3599))
                .executesPlayer((player, objects) -> {
                    int seconds = (int) objects[0];
                    LobbyPhase lobbyPhase = (LobbyPhase) GameStateManager.INSTANCE.getPhase();
                    if (PermissionUtils.checkForHigherRank(player)) {
                        player.sendMessage(t("hglabor.higherRankIsOnline", ChatUtils.locale(player)));
                        return;
                    }
                    if (lobbyPhase.isStarting()) {
                        lobbyPhase.prepareToWait();
                    }
                    lobbyPhase.setMaxPhaseTime(seconds);
                    GameStateManager.INSTANCE.setTimer((lobbyPhase.getMaxPhaseTime() - seconds));
                    Logger.debug(String.format("%s has extended", player.getName()));
                })
                .register();
    }
}
