package com.leclowndu93150.thaumcraft.api.golems;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/**
 * A behavioral tag carried by golem materials and parts. The union of all part traits,
 * with opposing pairs cancelling each other, defines what a golem can do.
 *
 * @since 1.0.0
 */
public enum GolemTrait implements StringRepresentable {
    SMART,
    DEFT,
    CLUMSY,
    FIGHTER,
    WHEELED,
    FLYER,
    CLIMBER,
    HEAVY,
    LIGHT,
    FRAGILE,
    REPAIR,
    SCOUT,
    ARMORED,
    BRUTAL,
    FIREPROOF,
    BREAKER,
    HAULER,
    RANGED,
    BLASTPROOF;

    /** Codec serializing the trait by its lower-case name. */
    public static final Codec<GolemTrait> CODEC = StringRepresentable.fromEnum(GolemTrait::values);

    private final String serializedName;
    private final Identifier icon;
    private GolemTrait opposite;

    GolemTrait() {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.icon = Identifier.fromNamespaceAndPath("thaumcraft", "textures/misc/golem/tag_" + serializedName + ".png");
    }

    /**
     * @return the icon texture drawn for this trait in golem UIs
     */
    public Identifier icon() {
        return icon;
    }

    /**
     * @return the trait this trait cancels and is cancelled by, or null when it has none
     */
    public @Nullable GolemTrait opposite() {
        return opposite;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    static {
        CLUMSY.opposite = DEFT;
        DEFT.opposite = CLUMSY;
        HEAVY.opposite = LIGHT;
        LIGHT.opposite = HEAVY;
        FRAGILE.opposite = ARMORED;
        ARMORED.opposite = FRAGILE;
    }
}
