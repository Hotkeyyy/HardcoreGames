package de.hglabor.plugins.hardcoregames.game.mechanics.recraft;

import de.hglabor.plugins.hardcoregames.player.HGPlayer;
import de.hglabor.plugins.hardcoregames.player.PlayerList;

import static de.hglabor.utils.localization.Localization.t;

public class RecraftInspector {
    private final int maxRecraftAmount;

    public RecraftInspector(int maxRecraftAmount) {
        this.maxRecraftAmount = maxRecraftAmount;
    }

    public void tick() {
        for (HGPlayer hgPlayer : PlayerList.INSTANCE.getAlivePlayers()) {
            hgPlayer.getBukkitPlayer().ifPresent(player -> {
                hgPlayer.getRecraft().calcRecraft(player.getInventory().getContents());
                if (hgPlayer.getRecraft().getRecraftPoints() > maxRecraftAmount) {
                    player.sendMessage(t("recraftNerf.tooMuch", hgPlayer.getLocale()));
                    while (hgPlayer.getRecraft().getRecraftPoints() > maxRecraftAmount) {
                        hgPlayer.getRecraft().decrease(player, 1);
                    }
                }
            });
        }
    }
}
