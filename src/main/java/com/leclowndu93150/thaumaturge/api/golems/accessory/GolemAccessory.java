package com.leclowndu93150.thaumaturge.api.golems.accessory;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * A wearable golem accessory applied by using its item on a golem. Accessories stack freely
 * except within an exclusion group, of which a golem may wear at most one.
 *
 * <p>Stat fields modify the wearing golem: {@code healthBonus} and {@code armorBonus} add flat
 * points, {@code rangeFactor} and {@code speedFactor} multiply sight range and movement speed,
 * and {@code regenFactor} multiplies the self-repair interval, so values below 1 heal faster.
 * When {@code killCredit} is set, entities slain by the golem count as kills by its owner.
 *
 * @param id the accessory id, unique across all registered accessories
 * @param group the exclusion group, or {@link Group#NONE} for unrestricted accessories
 * @param healthBonus flat max health added while worn
 * @param rangeFactor multiplier on the golem's sight and work range
 * @param speedFactor multiplier on the golem's movement speed
 * @param regenFactor multiplier on the golem's self-repair interval
 * @param armorBonus flat armor added while worn
 * @param killCredit whether the golem's kills are credited to its owner
 * @param behavior server-side behaviour with its own saved state, or empty for a stat-only
 *                 accessory
 * @since 1.0.0
 */
public record GolemAccessory(Identifier id, Group group, int healthBonus, float rangeFactor, float speedFactor, float regenFactor, int armorBonus, boolean killCredit,
        Optional<GolemAccessoryBehavior<?>> behavior) {
    /**
     * Validates the components.
     *
     * @throws NullPointerException when {@code id}, {@code group} or {@code behavior} is null
     */
    public GolemAccessory {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(behavior, "behavior");
    }

    /**
     * Creates a stat-only accessory with no behaviour.
     *
     * @param id the accessory id
     * @param group the exclusion group
     * @param healthBonus flat max health added while worn
     * @param rangeFactor multiplier on sight and work range
     * @param speedFactor multiplier on movement speed
     * @param regenFactor multiplier on the self-repair interval
     * @param armorBonus flat armor added while worn
     * @param killCredit whether kills are credited to the owner
     */
    public GolemAccessory(Identifier id, Group group, int healthBonus, float rangeFactor, float speedFactor, float regenFactor, int armorBonus, boolean killCredit) {
        this(id, group, healthBonus, rangeFactor, speedFactor, regenFactor, armorBonus, killCredit, Optional.empty());
    }

    /**
     * Exclusion groups for accessories occupying the same spot on a golem.
     *
     * @since 1.0.0
     */
    public enum Group {
        NONE, HAT, EYES
    }
}
