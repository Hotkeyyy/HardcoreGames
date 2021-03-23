package de.hglabor.plugins.hardcoregames.command;

import de.hglabor.plugins.hardcoregames.HardcoreGames;
import de.hglabor.plugins.hardcoregames.game.GameStateManager;
import de.hglabor.plugins.hardcoregames.game.PhaseType;
import de.hglabor.plugins.hardcoregames.game.phase.IngamePhase;
import de.hglabor.utils.noriskutils.ChatUtils;
import de.hglabor.utils.noriskutils.feast.Feast;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.hglabor.utils.localization.Localization.t;

public class FeastCommand {
    private final Particle particle;
    private final Sound sound;
    private final int particleAmount;
    private final double space;
    private final Map<UUID, ParticleSpawner> particleSpawners;

    public FeastCommand() {
        this.particle = Particle.WATER_DROP;             //Todo nicer particle?
        this.sound = Sound.BLOCK_NOTE_BLOCK_PLING;       //TODO better sound
        this.particleAmount = 100;
        this.space = 2D;
        this.particleSpawners = new HashMap<>();
        new CommandAPICommand("feast")
                .withRequirement(commandSender -> GameStateManager.INSTANCE.getPhase().getType().equals(PhaseType.INGAME))
                .executesPlayer((player, objects) -> {
                    IngamePhase ingamePhase = (IngamePhase) GameStateManager.INSTANCE.getPhase();
                    Feast feast = ingamePhase.getFeast();
                    if (feast == null) {
                        player.sendMessage(t("feast.notSpawnedYet", ChatUtils.locale(player)));
                        return;
                    }
                    Location toLocation = player.getWorld().getSpawnLocation().clone().add(0, 5, 0);
                    Location startPoint = player.getEyeLocation();
                    double distance = startPoint.distance(toLocation);
                    Vector p1 = startPoint.toVector();
                    Vector p2 = toLocation.toVector();
                    Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
                    if (particleSpawners.containsKey(player.getUniqueId())) {
                        ParticleSpawner particleSpawner = particleSpawners.get(player.getUniqueId());
                        particleSpawner.cancel();
                    }
                    ParticleSpawner particleSpawner = new ParticleSpawner(player, distance, p1, vector);
                    particleSpawner.runTaskTimer(HardcoreGames.getPlugin(), 0, 5);
                    particleSpawners.put(particleSpawner.player.getUniqueId(), particleSpawner);
                    player.sendMessage(t("feast.pointingTowardsFeast", ChatUtils.locale(player)));
                })
                .register();
    }

    private class ParticleSpawner extends BukkitRunnable {
        private final Player player;
        private final Vector start;
        private final Vector vector;
        private final double distance;
        private double length;

        public ParticleSpawner(Player player, double distance, Vector start, Vector vector) {
            this.distance = distance;
            this.player = player;
            this.start = start;
            this.vector = vector;
        }

        @Override
        public void run() {
            if (length < distance) {
                start.add(vector);
                player.playSound(start.toLocation(player.getWorld()), sound, 1, 1);
                player.spawnParticle(particle, start.getX(), start.getY(), start.getZ(), particleAmount);
                length += space;
            } else {
                particleSpawners.remove(player.getUniqueId());
                cancel();
            }
        }
    }
}
