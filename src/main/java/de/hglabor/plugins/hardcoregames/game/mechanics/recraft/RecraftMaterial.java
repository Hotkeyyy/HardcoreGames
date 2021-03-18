package de.hglabor.plugins.hardcoregames.game.mechanics.recraft;

import org.bukkit.Material;

import java.util.*;

public class RecraftMaterial {
    private final Map<Material, Integer> materials;
    private final int maxSoupAmount;

    public RecraftMaterial(int getMaxSoupAmount, Material... materials) {
        this.materials = new HashMap<>();
        this.maxSoupAmount = getMaxSoupAmount;
        Arrays.stream(materials).forEach(material -> this.materials.put(material, 0));
    }

    public float getPoints() {
        return materials.getOrDefault(getLowestMaterial(), 0);
    }

    public void decrease(Material material, int amount) {
        materials.put(material, materials.get(material) - amount);
    }

    public Set<Material> getMaterials() {
        return materials.keySet();
    }

    public int get(Material material) {
        return getOrDefault(material, 0);
    }

    public int getOrDefault(Material material, int fallback) {
        return materials.getOrDefault(material, fallback);
    }

    public void put(Material material, int zahl) {
        materials.put(material, zahl);
    }

    public Material getLowestMaterial() {
        if (materials.size() > 1) {
            if (materials.values().stream().anyMatch(integer -> integer == 0)) {
                return null;
            }
            Optional<Map.Entry<Material, Integer>> materialIntegerEntry = materials.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue));
            return materialIntegerEntry.map(Map.Entry::getKey).orElse(null);
        } else {
            return materials.keySet().stream().findFirst().orElse(null);
        }
    }

    public float getMaterialValue() {
        return (float) maxSoupAmount / materials.size();
    }

    public void reset() {
        materials.replaceAll((m, v) -> 0);
    }
}
