package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;

public final class GolemStats {
    private static final double BASE_HEALTH = 10.0D;
    private static final double FRAGILE_FACTOR = 0.75D;
    private static final double ARMORED_FACTOR = 1.5D;
    private static final double ARMORED_BONUS = 1.0D;
    private static final double BRUTAL_FACTOR = 1.5D;
    private static final double BRUTAL_BONUS = 1.0D;
    private static final double DAMAGE_PER_RANK = 0.25D;

    private GolemStats() {}

    public static double health(IGolemProperties properties) {
        double health = BASE_HEALTH + properties.material().healthMod();
        return properties.hasTrait(TTGolemTraits.FRAGILE.get()) ? Math.floor(health * FRAGILE_FACTOR) : health;
    }

    public static double armor(IGolemProperties properties) {
        double armor = properties.material().armor();
        if (properties.hasTrait(TTGolemTraits.ARMORED.get())) {
            armor = Math.floor(Math.max(armor * ARMORED_FACTOR, armor + ARMORED_BONUS));
        }
        if (properties.hasTrait(TTGolemTraits.FRAGILE.get())) {
            armor = Math.floor(armor * FRAGILE_FACTOR);
        }
        return armor;
    }

    public static double meleeDamage(IGolemProperties properties) {
        if (!properties.hasTrait(TTGolemTraits.FIGHTER.get())) {
            return 0.0D;
        }
        double damage = properties.material().damage();
        if (properties.hasTrait(TTGolemTraits.BRUTAL.get())) {
            damage = Math.max(damage * BRUTAL_FACTOR, damage + BRUTAL_BONUS);
        }
        return damage + DAMAGE_PER_RANK * properties.rank();
    }
}
