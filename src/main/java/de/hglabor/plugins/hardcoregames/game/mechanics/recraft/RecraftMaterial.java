package de.hglabor.plugins.hardcoregames.game.mechanics.recraft;

import org.bukkit.Material;

import java.util.*;

public class RecraftMaterial extends HashMap<Material,Integer> {
    private final int maxSoupAmount;

    public RecraftMaterial(int getMaxSoupAmount, Material... materials) {
        this.maxSoupAmount = getMaxSoupAmount;
        Arrays.stream(materials).forEach(material -> put(material, 0));
    }

    public float getPoints() {
        return getOrDefault(getLowestMaterial(), 0);
    }

    public void decrease(Material material, int amount) {
        put(material, get(material) - amount);
    }

    public Material getLowestMaterial() {
        if (size() > 1) {
            if (values().stream().anyMatch(integer -> integer == 0)) {
                return null;
            }
            Optional<Map.Entry<Material, Integer>> materialIntegerEntry = entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue));
            return materialIntegerEntry.map(Map.Entry::getKey).orElse(null);
        } else {
            return keySet().stream().findFirst().orElse(null);
        }
    }

    public float getMaterialValue() {
        return (float) maxSoupAmount / size();
    }

    public void reset() {
        replaceAll((m, v) -> 0);
    }
}
